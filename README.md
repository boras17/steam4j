# steam marketplace library

# Usage examples

Sniping steam marekt actions:
  First you have to create new instance of java 11 HttpClient:
 HttpClient simpleClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();

TODO:
  Refractor RSA hashing class for for steam password
  Service for making steam trading requests
  refractor SteamLogin(steam guard code fetcher move to email service)
