
// Perform a get request and execute the callback
var getTweet = function(id, blackList, callback) {
    $.get("rest/timeline",
          {id : id, blackList : blackList},
          callback);
}


var renderTweet = function(tweet) {
    $("#tweet-list").append('<li>' + tweet + '</li>');
}


$(document).ready(function() {

    $("#get_timeline").click(function() {
        console.log("Getting user timeline");

        $("#initial").hide();

        var renderTweets = function(data) {
            console.log("Got tweets: ");
            console.log(data);
            var tweets = data["tweets"];
            $("#tweet-list").empty();
            for (var i=0; i < tweets.length; ++i) {
                console.log("Rendering tweet: " + tweets[i]);
                renderTweet(tweets[i]);
            }
            $("#tweet").show();
        }

        var twitterId = $("#twitter-id").val();
        var blackList = $("#black-list").val();

        console.log("Getting tweet from id: " + twitterId);

        getTweet(twitterId, blackList, renderTweets);

        return false;
    });
});
