public class Driver
{
    public static void main(String[] args)
    {
        AuthorisationServer authorisationServer = new AuthorisationServer();
        ResourceServer resourceServer = new ResourceServer();
        Client client = new Client();

        authorisationServer.start();
        resourceServer.start();
        client.start();
    }
}
