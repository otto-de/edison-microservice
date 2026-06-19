import {formatUTCToLocalTime, formatUTCToLocalDateTime, formatInitialDates} from './datetime.js'

export function getLog(logIndex) {
    $.ajax({
        type: "GET",
        url: $('.logWindow').data("job-url"),
        headers: {
            Accept: "application/json",
            "Content-Type": "application/json"
        },
        data: {},
        dataType: "json",
        error: function (data, status, error) {
            console.log("Error polling job status.");
            var jobStatus = $('#job-status');
            jobStatus.attr("class", "badge bg-danger");
            jobStatus.attr("style", "width:10em; height:2em;");
            jobStatus.html("<span>UNKNOWN</span>");
        },
        success: function (data, textStatus, xhr) {
            var numberOfMessages = data.messages.length;
            var logWindow = $('.logWindow');

            while (logIndex < numberOfMessages) {
                if (logIndex === 0) {
                    logWindow.empty();
                }
                var msg = data.rawMessages[logIndex];
                var div = document.createElement("div");
                var tsSpan = document.createElement("span");
                tsSpan.textContent = "[" + formatUTCToLocalTime(msg.timestampUTCIsoString) + "]";
                var msgSpan = document.createElement("span");
                msgSpan.textContent = "[" + msg.level + "] " + msg.message;
                div.appendChild(tsSpan);
                div.appendChild(document.createTextNode(" "));
                div.appendChild(msgSpan);
                logWindow[0].appendChild(div);
                logIndex++;
            }

            if ($('#follow-log').prop('checked')) {
                logWindow.each(function () {
                    var scrollHeight = Math.max(this.scrollHeight, this.clientHeight);
                    this.scrollTop = scrollHeight - this.clientHeight;
                });
            }

            //Schedule further polling if still runnin'
            if (data.state === 'Running') {
                setTimeout(function () {
                    getLog(logIndex)
                }, 2000);
            } else {
                var jobStatus = $('#job-status');
                if (data.status === 'OK') {
                    jobStatus.attr("class", "badge bg-success");
                    jobStatus.html("<span>" + data.status + "</span>");
                } else if (data.status === 'SKIPPED') {
                    jobStatus.attr("class", "badge bg-secondary");
                    jobStatus.html("<span>" + data.status + "</span>");
                } else if (data.status === 'ERROR') {
                    jobStatus.attr("class", "badge bg-danger");
                    jobStatus.html("<span>" + data.status + "</span>");
                } else if (data.status === 'DEAD') {
                    jobStatus.attr("class", "badge bg-warning");
                    jobStatus.html("<span>" + data.status + "</span>");
                }
                $("#job-stopped").text(formatUTCToLocalDateTime(data.stoppedIso));
                $(".triggerButton").prop('disabled', false);
            }
            $("#job-last-updated").text(formatUTCToLocalDateTime(data.lastUpdatedIso));
        }
    });
}

if (typeof window !== 'undefined' && !window.__testing__) {
    //Uncheck follow log checkbox if real mouse scrolling detected
    $(".logWindow").bind("scroll mousedown DOMMouseScroll mousewheel keyup", function (e) {
        if (e.which > 0 || e.type === "mousedown" || e.type === "mousewheel") {
            $("#follow-log").prop('checked', false);
        }
    });

    formatInitialDates();
    setTimeout(function () {
        getLog(0)
    }, 1000);
}