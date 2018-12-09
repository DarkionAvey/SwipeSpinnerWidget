<img src="/preview.gif" width="300" align="right" alt="SwipeSpinnerWidget demo" hspace="20">
<h1>SwipeSpinnerWidget</h1>
<a href="https://github.com/DarkionAvey/SwipeSpinnerWidget/blob/7cf7405b88329fed83e15261fb80f40dd4504e4d/app/release/app-release.apk?raw=true">Download Demo APK (for Android Lollipop+)</a><br/><br/>


<p>A spinner which allows the user to scroll the whole list through a single
 dragging gesture. This is a helper class which binds to an existing recycler
 view. The use of recycler view ensures better performance and
 better long-term support.
 It supports both vertical and horizontal scrolling;
 set the orientation by using LinearLayoutManager.setOrientation(int).
 Customization is not included in the helper class; check the companion demo app to see
how to add indication arrows and for other tricks.</p>
<h3>Compatibilty</h3>
The helper class is backwards compatible the same way as RecyclerView. The demo app is Lollipop+ due to vector drawables.
<h3>Credits</h3>
Based on <a href="https://www.uplabs.com/posts/stepper-xvi">original UX design</a> by Oleg Frolov
<h3>Include it in your app</h3>
<p>Copy <a href="https://raw.githubusercontent.com/DarkionAvey/SwipeSpinnerWidget/master/app/src/main/java/net/darkion/swipespinner/SwipeSpinnerHelper.java">SwipeSpinnerHelper.Java</a> class to your app. That's it!</p>
<h3>How to use</h3>
Bind recycler view object to SwipeSpinnerHelper class using static method
 
```java
SwipeSpinnerHelper.bindRecyclerView(recyclerView)
```
You can use this with any recyclerview as long as you use linear layout manager.
