// OneID IDENTITY PROVIDER.

import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.*;
import java.time.*;
import java.util.*;

public class AuthorisationServer
{
    private HttpServer server;

    private Map<String, String> users = new HashMap<>();

    private Map<String /* code */, Date /* expiration date */> authorisationCodeExpirationTimes = new HashMap<>();
    private Map<String /* code */, String /* clientID */> authorisationCodeClients = new HashMap<>();
    private Map<String /* code */, String /* username */> authorisationCodeUsers = new HashMap<>();

    private Set<String /* token */> accessTokens = new HashSet<>();
    private Map<String /* token */, Date /* expiration date */> accessTokenExpirationTimes = new HashMap<>();
    private Map<String /* token */, String /* clientID */> accessTokenClients = new HashMap<>();
    private Map<String /* token */, String /* username */> accessTokenUsers = new HashMap<>();

    private Map<String /* clientID */, URL> clientRedirectionUrls = new HashMap<>();

    private static final long MILLISECONDS_PER_MINUTE = 60000;
    private static final long MILLISECONDS_PER_HOUR = 3600000;

    public AuthorisationServer()
    {
        try
        {
            registerClients();
            registerUsers();

            server = HttpServer.create(new InetSocketAddress(8080), 0);

            setUpAuthorisationEndpoint();
            setUpTokenEndpoint();
            setUpValidationEndpoint();

            server.setExecutor(null);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void registerClients() throws MalformedURLException
    {
        clientRedirectionUrls.put("playlistrSecret", new URL("http://localhost:8082/authenticated"));
    }

    private void registerUsers()
    {
        users.put("Alice", "a");
        users.put("Bob", "b");
    }

    private void setUpAuthorisationEndpoint()
    {
        // Authorisation endpoint.
        server.createContext("/oauth2", request ->
        {
            Map<String, String> params = Utilities.getQueryParameters(request.getRequestURI().getQuery());

            switch (request.getRequestMethod().toUpperCase())
            {
                case "GET":
                    // STEP 3: Redirect to show HTML page with login form.

                    if (params.get("response_type").equals("code") && params.containsKey("client_id") && params.containsKey("scope"))
                        Utilities.html(request, "Assets/OneID.html");
                    else
                    {
                        request.sendResponseHeaders(400, 0);
                        request.getResponseBody().close();
                    }

                    break;

                case "POST":
                    // STEP 7: Authentication consent.
                    // STEP 8: Authenticate user (validate username/password).
                    String line, username = "", password = "";

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getRequestBody())))
                    {
                        StringBuilder builder = new StringBuilder();

                        while ((line = reader.readLine()) != null)
                            builder.append(line);

                        String[] entries = builder.toString().split("&");

                        for (String entry : entries)
                        {
                            if (entry.startsWith("username=")) username = entry.split("=")[1];
                            if (entry.startsWith("password=")) password = entry.split("=")[1];
                        }
                    }

                    if (users.containsKey(username) && users.get(username).equals(password))
                    {
                        // STEP 9: Generate authorisation code.
                        String clientId = params.get("client_id");
                        String authorisationCode = Utilities.randomString();

                        long t = new Date().getTime();
                        Date expirationDate = new Date(t + (10 * MILLISECONDS_PER_MINUTE));

                        authorisationCodeExpirationTimes.put(authorisationCode, expirationDate);
                        authorisationCodeClients.put(authorisationCode, clientId);
                        authorisationCodeUsers.put(authorisationCode, username);

                        if (clientRedirectionUrls.containsKey(clientId))
                        {
                            String redirectUri = clientRedirectionUrls.get(clientId) + "?code=" + authorisationCode;
                            request.getResponseHeaders().add("Location", redirectUri);
                            request.sendResponseHeaders(302, 0);
                            request.getResponseBody().close();
                        }
                        else
                        {
                            request.sendResponseHeaders(401, 0);
                            request.getResponseBody().close();
                        }
                    }
                    else
                    {
                        request.sendResponseHeaders(401, 0);
                        request.getResponseBody().close();
                    }

                    break;
            }
        });
    }

    private void setUpTokenEndpoint()
    {
        // Token endpoint.
        server.createContext("/token", request ->
        {
            Map<String, String> params = Utilities.getQueryParameters(request.getRequestURI().getQuery());

            if (params.containsKey("code") && params.containsKey("client_id"))
            {
                String clientId = params.get("client_id");
                String code = params.get("code");

                if (authorisationCodeClients.containsKey(code))
                {
                    String associatedClientId = authorisationCodeClients.get(code);
                    Date codeExpirationDate = authorisationCodeExpirationTimes.get(code);
                    boolean hasExpired = new Date().after(codeExpirationDate);
                    String username = authorisationCodeUsers.get(code);

                    // STEP 10: Get access token.
                    if (clientId.equals(associatedClientId) && !hasExpired)
                    {
                        // STEP 11: Generate access token.
                        String accessToken = Utilities.randomString();

                        long t = new Date().getTime();
                        Date tokenExpirationDate = new Date(t + (3 * MILLISECONDS_PER_HOUR));

                        accessTokens.add(accessToken);
                        accessTokenExpirationTimes.put(accessToken, tokenExpirationDate);
                        accessTokenClients.put(accessToken, clientId);
                        accessTokenUsers.put(accessToken, username);

                        String response = "{" +
                                "\"access_token\":\"" + accessToken + "\"," +
                                "\"token_type\":\"bearer\"," +
                                "\"expires_in\":\"10799\"" +
                                "}";

                        request.sendResponseHeaders(200, response.length());

                        OutputStream os = request.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    }
                    else
                    {
                        request.sendResponseHeaders(401, 0);
                        request.getResponseBody().close();
                    }
                }
                else
                {
                    request.sendResponseHeaders(400, 0);
                    request.getResponseBody().close();
                }
            }
            else
            {
                request.sendResponseHeaders(400, 0);
                request.getResponseBody().close();
            }
        });
    }

    private void setUpValidationEndpoint()
    {
        // Validation endpoint.
        server.createContext("/validate", request ->
        {
            if (request.getRequestMethod().toUpperCase().equals("POST"))
            {
                String line, accessToken = "";

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getRequestBody())))
                {
                    StringBuilder builder = new StringBuilder();

                    while ((line = reader.readLine()) != null)
                        builder.append(line);

                    String[] entries = builder.toString().split("&");

                    for (String entry : entries)
                        if (entry.startsWith("accessToken=")) accessToken = entry.split("=")[1];
                }

                if (accessTokens.contains(accessToken))
                {
                    Date tokenExpirationDate = accessTokenExpirationTimes.get(accessToken);
                    boolean hasExpired = new Date().after(tokenExpirationDate);
                    String clientID = accessTokenClients.get(accessToken);
                    String username = accessTokenUsers.get(accessToken);

                    if (!hasExpired)
                    {
                        String response = "{" +
                                "\"username\":\"" + username + "\"" +
                                "}";

                        request.sendResponseHeaders(200, response.length());

                        OutputStream os = request.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    }
                    else
                    {
                        request.sendResponseHeaders(401, 0);
                        request.getResponseBody().close();
                    }
                }
                else
                {
                    request.sendResponseHeaders(401, 0);
                    request.getResponseBody().close();
                }
            }
            else
            {
                request.sendResponseHeaders(405, 0);
                request.getResponseBody().close();
            }
        });
    }

    public void start()
    {
        server.start();
    }
}
