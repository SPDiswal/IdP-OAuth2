// IDENTITY PROVIDER.
// OneID.

import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

public class AuthorisationServer
{
    private HttpServer server;
    private Set<String> clients = new HashSet<>();
    private Map<String, Void /* TODO */> accessTokens = new HashMap<>();

    public AuthorisationServer()
    {
        clients.add("playlistrSecret");

        try
        {
            server = HttpServer.create(new InetSocketAddress(8080), 0);

            setUpAuthenticate();
            setUpGenerateAccessToken();

            server.setExecutor(null);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void setUpAuthenticate()
    {
        server.createContext("/oauth2", request ->
        {
            Map<String, String> params = Utilities.getQueryParameters(request.getRequestURI().getQuery());

            switch (request.getRequestMethod().toUpperCase())
            {
                case "GET":
                    // STEP 3: Redirect to show HTML page with login form.

                    if (params.get("response_type").equals("code")
                            && params.containsKey("client_id")
                            && params.containsKey("scope"))
                    {
                        File file = new File("Assets/OneID.html");
                        request.sendResponseHeaders(200, file.length());

                        try (FileInputStream fileInputStream = new FileInputStream(file);
                             OutputStream os = request.getResponseBody())
                        {
                            byte[] buffer = new byte[(int) file.length()];
                            int count = 0;

                            while ((count = fileInputStream.read(buffer)) >= 0)
                                os.write(buffer, 0, count);
                        }
                    }
                    else
                        request.sendResponseHeaders(400, 0);

                    break;

                case "POST":
                    // STEP 7: Authentication consent.
                    // STEP 8: Authenticate user (validate username/password).
                    // STEP 9: Generate authorisation code.

                    String redirectUri = params.get("redirect_uri") + "?authorisationCode=" + "BOGUS";  // TODO Manage authorisation code.
                    request.getResponseHeaders().add("Location", redirectUri);

                    break;
            }
        });
    }

    private void setUpGenerateAccessToken()
    {
        // STEP 10: Get access token.
        server.createContext("/token", request ->
        {
            // STEP 11: Generate access token.

            Map<String, String> params = Utilities.getQueryParameters(request.getRequestURI().getQuery());

            if (params.get("authorisationCode").equals("BOGUS") && params.get("client_id").equals("playlistrSecret"))
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

    public void start()
    {
        server.start();
    }
}
