# steam marketplace library

# Usage examples

Sniping steam marekt actions:
  First you have to create new instance of java 11 HttpClient:
  ```java 
 HttpClient simpleClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
  ```
  Now if you have client instance, create new Instance of SteamMarketplace class and pass your client as a constructor parameter like below:
 ```java 
  SteamMarketplace fetcher = new SteamMarketplace(simpleClient);
 ```
 In order to scrapping market actions you have to extract item name id from marketplace via getItemNameIdByItemName(itemName:str, game:Game) method:
  ```java 
 Optional<Integer> itemNameIdOptional = fetcher.getItemNameIdByItemName("Mann Co. Supply Crate Key", Game.COUNTER_STRIKE);
  ```
  first parameter represents item name and second parameter is enum which represents specified game related with item.
  # Quick tutorial how to get item name
  Firstly go to the stem market place and select specified item. You will see URL like this one: 
  https://steamcommunity.com/market/listings/440/Mann%20Co.%20Supply%20Crate%20Key
  now copy the last part of this url(in this situation it will be this part: Mann%20Co.%20Supply%20Crate%20Key)
  and then go to: https://meyerweb.com/eric/tools/dencoder/ and decode this part, the result of this will be item name in plain text.
  
  Now when you went through steps above you can start snipping items:
   ```java 
fetcher.addSnipedItem(ItemSnipingData.builder()
        .itemNameId(14962905)
        .breakTimeBetweenRequestsInMillis(100)
        .snipeCriteria(new SnipeCriteria(new Predicate<BuySellSignal>() {
                    @Override
                    public boolean test(BuySellSignal buySellSignal) {

                        return true;
                    }
                })).build());
```
addSnipedItem(itemSnipingData) accepts ItemSnipingData object and this class has few important properties which you can specify via builder methods:
itemNameId(int) - it is just item name id
breakTimeBetweenRequestsInMillis(long) - accepts long type parameter which represents delay between two different requests to the steam marketplace
snipeCriteria(SnipeCriteria) - here you can pass SnipeCriteria class where constructor parameter is simple predicate which provide BuySellSignall class 
and returns true if this signal should be delegated to the notifier. 
# What is notifier?
Notifier is class which implements ActivityNotificationStrategy:
 ```java 
void handleNotification(BuySellSignal activityForItem);
  ```
 and method handleNotification is called when SnipeCritera Predicate returns true. You can implement your own notifier. For exmaple let's implement simple
 console notifier:
  ```java 
class ConsoleNotifier implements ActivityNotificationStrategy{
      @Override
      public void handleNotification(BuySellSignal activityForItem) {
          System.out.println(activityForItem);
      }
}
```
and then you can pass instance into Notifier class constructor:
```java 
Notifier notifier = new Notifier(new ConsoleNotifier());
```
then in order to regsiter this notifier and start sniping item call:
```java 
fetcher.startSniping(Executors.newSingleThreadExecutor(), notifier);
```
#Logi in
In order to request secured steam endpoints you have to create sessionClient for this requests, you can do this via SteamLogin class lik below:
```java
SteamLogin steamLogin = new StreamLogin(username, password);
steamLogin.login();

String steamId = steamLogin.getUserId();
HttpClient sessionClient = steamLogin.getSessionClient();
```
And that is all you need but if you want to handle steam guard tokens on your email then pass your email configuration into constructor as third parameter:
SteamLogin(username, password, emailConfiguration)
you can simply create email configuration with EmailConfigurationFactory. 
Example:
```java
EmailCredentials credentials = new EmailCredentials(email, password);
EmailConfigurationFactory.defaultGmailImapConfiguration(credentials);
```
where first parameter is ExecutorService
In progress:
  Refractor RSA hashing class for for steam password
  Service for making steam trading requests
  refractor SteamLogin(steam guard code fetcher move to email service)
