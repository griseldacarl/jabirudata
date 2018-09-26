# jabiru

A [re-frame](https://github.com/Day8/re-frame) application designed to ... well, that part is up to you.

## Development Mode

### Run application:

```
lein clean
lein figwheel dev
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

## Production Build


To compile clojurescript to javascript:

```
lein clean
lein cljsbuild once min

```

<script src="https://www.gstatic.com/firebasejs/5.1.0/firebase.js"></script>
<script>
  // Initialize Firebase
  var config = {
    apiKey: "AIzaSyDH2BL89dq3MXiTue-HZVXqBY0rVCTfC0k",
    authDomain: "jabirudata.firebaseapp.com",
    databaseURL: "https://jabirudata.firebaseio.com",
    projectId: "jabirudata",
    storageBucket: "",
    messagingSenderId: "244548270032"
  };
  firebase.initializeApp(config);
</script>
