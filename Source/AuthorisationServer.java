// OneID IDENTITY PROVIDER.

import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.*;
import java.util.*;

public class AuthorisationServer
{
    private HttpServer server;
    private Set<String> clients = new HashSet<>();

    private Map<String, String> accessTokens = new HashMap<>();
    private Map<String, Date> accessTokenExpirationTimes = new HashMap<>();

    private Map<String, String> authorisationCodes = new HashMap<>();
    private Map<String, Date> authorisationCodeExpirationTimes = new HashMap<>();

    private Map<String, URL> clientRedirectionUrls = new HashMap<>();

    public AuthorisationServer()
    {
        try
        {
            registerClients();

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
                        request.sendResponseHeaders(400, 0);

                    break;

                case "POST":
                    // STEP 7: Authentication consent.

                    // STEP 8: Authenticate user (validate username/password).

                    // STEP 9: Generate authorisation code.
                    String authorisationCode = Utilities.randomString();



                    String redirectUri = params.get("redirect_uri") + "?code=" + "BOGUS";  // TODO Manage authorisation code.
                    request.getResponseHeaders().add("Location", redirectUri);
                    request.sendResponseHeaders(302, 0);
                    request.getResponseBody().close();

                    break;
            }
        });
    }

    private void setUpTokenEndpoint()
    {
        // Token endpoint.
        server.createContext("/token", request ->
        {
            // STEP 10: Get access token.
            // STEP 11: Generate access token.

            Map<String, String> params = Utilities.getQueryParameters(request.getRequestURI().getQuery());

            if (params.get("code").equals("BOGUS") && params.get("client_id").equals("playlistrSecret"))
            {
                String response = "{" +
                        "\"access_token\":\"bogusAccess\"," +
                        "\"token_type\":\"bearer\"," +
                        "\"expires_in\":\"1337\"" +
                        "}";

                // TODO: Polish access token.

                request.sendResponseHeaders(200, response.length());

                OutputStream os = request.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
            else
                request.sendResponseHeaders(400, 0);
        });
    }

    private void setUpValidationEndpoint()
    {
        // Token endpoint.
        server.createContext("/validate", request ->
        {
            // POST.
        });
    }

    public void start()
    {
        server.start();
    }
}
