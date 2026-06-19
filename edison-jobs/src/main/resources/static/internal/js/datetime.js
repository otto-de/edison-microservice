export function formatUTCToLocalDateTime(datetime) {
    if (datetime == null || datetime === "") {
        return "-";
    }
    const parsedDatetime = Date.parse(datetime);
    const utcTime = new Date();
    utcTime.setTime(parsedDatetime);
    const timezoneOffset = (new Date()).getTimezoneOffset() * 60000; //offset in milliseconds
    return (new Date(utcTime - timezoneOffset)).toISOString().slice(0, -5).replace("T", " ");
}

export function formatUTCToLocalTime(datetime) {
    if (datetime == null || datetime === "") {
        return "-";
    }
    return formatUTCToLocalDateTime(datetime).slice(11);
}

export function formatInitialDates() {
    const dateAndTimeNodes = document.querySelectorAll(".job-started, .job-stopped, .job-updated");
    for (const dateNode of dateAndTimeNodes) {
        $(dateNode).text(formatUTCToLocalDateTime($(dateNode).data("datetime")));
    }
    const timeNodes = document.querySelectorAll(".job-messagedate");
    for (const dateNode of timeNodes) {
        $(dateNode).text("[" + formatUTCToLocalTime($(dateNode).data("datetime")) + "]");
    }
}