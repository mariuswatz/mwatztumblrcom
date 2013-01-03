<script type='text/javascript'>
// Google Analytics - only supposed to track if URL isn't 'file://' etc
var doTrack=1;
if(window.location.href.indexOf('file:')>-1) doTrack=-1;
//alert("doTrack "+doTrack+" "+window.location.href);

if(doTrack>0) {
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
var gaString=unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E");
document.write(gaString);
//alert(gaString);
}
</script>
<script type="text/javascript">
if(doTrack>0) {
//alert("tracking");
var pageTracker = _gat._getTracker("UA-221130-4");
pageTracker._initData();
pageTracker._trackPageview();
}
</script>