function formatUTCToLocalDateTime(datetime) {
    if (datetime == null || datetime === "") {
        return "-";
    }
    var parsedDatetime = Date.parse(datetime);
    var utcTime = new Date();
    utcTime.setTime(parsedDatetime);
    var timezoneOffset = (new Date()).getTimezoneOffset() * 60000; //offset in milliseconds
    return (new Date(utcTime - timezoneOffset)).toISOString().slice(0, -5).replace("T", " ");
}

function formatUTCToLocalTime(datetime) {
    if (datetime == null || datetime === "") {
        return "-";
    }
    return formatUTCToLocalDateTime(datetime).slice(11);
}

function formatInitialDates() {
    let dateAndTimeNodes = document.querySelectorAll(".job-started, .job-stopped, .job-updated");
    for (const dateNode of dateAndTimeNodes) {
        $(dateNode).text(formatUTCToLocalDateTime($(dateNode).data("datetime")));
    }
    let timeNodes = document.querySelectorAll(".job-messagedate");
    for (const dateNode of timeNodes) {
        $(dateNode).text("[" + formatUTCToLocalTime($(dateNode).data("datetime")) + "]");
    }
}