<img src="/preview.gif" width="300" align="right" alt="SwipeSpinnerWidget demo" hspace="20">
<h1>SwipeSpinnerWidget</h1>

 <p>A spinner which allows the user to scroll the whole list through a single
 dragging gesture. This is a helper class which binds to an existing recycler
 view. The use of recycler view ensures better performance and
 better long-term support.
 It supports both vertical and horizontal scrolling;
 set the orientation by using LinearLayoutManager.setOrientation(int).
 Customization is not included in the helper class; check the companion demo app to see
how to add indication arrows and for other tricks.</p>

<h3>Include it in your app</h3>
<p>Copy <a href="https://raw.githubusercontent.com/DarkionAvey/SwipeSpinnerWidget/master/app/src/main/java/net/darkion/swipespinner/SwipeSpinnerHelper.java">SwipeSpinnerHelper.Java</a> class to your app. That's it!</p> <br>
<h3>How to use</h3>
Bind recycler view object to SwipeSpinnerHelper class using static method
 
```java
SwipeSpinnerHelper.bindRecyclerView(recyclerView)
```
You can use this with any recyclerview as long as you use linear layout manager.
<h3>Credits</h3>
Based on <a href="https://www.uplabs.com/posts/stepper-xvi">original UX design</a> by Oleg Frolov
