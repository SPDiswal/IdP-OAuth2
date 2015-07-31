// IDENTITY PROVIDER.
// OneID.

import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;

public class AuthorisationServer
{
    private HttpServer server;

    public AuthorisationServer()
    {
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
            switch (request.getRequestMethod().toUpperCase())
            {
                case "GET":
                    // STEP 3: Redirect to show HTML page with login form.

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

                    break;

                case "POST":
                    // STEP 7: Authentication consent.
                    // STEP 8: Authenticate user (validate username/password).
                    // STEP 9: Generate authorisation code.

                    break;
            }
        });
    }

    private void setUpGenerateAccessToken()
    {
        // STEP 10: Get access token.
        server.createContext("/access", request ->
        {
            // STEP 11: Generate access token.

            String response = "This is the response";

            request.sendResponseHeaders(200, response.length());
            OutputStream os = request.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });
    }

    public void start()
    {
        server.start();
    }
}
