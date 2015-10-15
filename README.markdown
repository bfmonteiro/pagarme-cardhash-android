pagarme-cardhash-android
========================

A very simple Android lib to generate [Pagar.me](https://pagar.me/) card hashs.

Installation
------------

To install, just copy the CardHash class into your project, update the package declaration, and that's it. Actually, no; this lib uses [Volley](https://developer.android.com/training/volley/index.html), so you'll need to have that installed too.

Better install options coming soon.

Usage
-----

Put your Pagar.me encryption key on you manifest:

```xml
<meta-data
    android:name="me.pagar.EncryptionKey"
    android:value="my_encryption_key" />
```

Then do something like this:

```java
CardHash card = new CardHash();
card.holderName = "Some Name";
card.number = "4111111111111111"; // no whitespaces
card.expirationDate = "0817";
card.cvv = "123";
card.generate(context, new CardHash.Listener() {
    @Override
    public void onSucess(String cardHash) {
        // do your thing
    }
    @Override
    public void onError(Exception e) {
        if (e instanceof VolleyError) {
          // show network error message
        } else {
          // show strange error message
        }
    }
});
```

License
-------

MIT/X11
