function getLog(logIndex) {
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
            jobStatus.attr("class", "label label-danger");
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
                logWindow.append("<div><span>[" + formatUTCToLocalTime(data.rawMessages[logIndex].timestampUTCIsoString) + "]</span> <span>[" + data.rawMessages[logIndex].level + "] " + data.rawMessages[logIndex].message + "</span></div>");
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
                    jobStatus.attr("class", "label label-success");
                    jobStatus.attr("style", "width:10em; height:2em;");
                    jobStatus.html("<span>" + data.status + "</span>");
                } else if (data.status === 'SKIPPED') {
                    jobStatus.attr("class", "label label-default");
                    jobStatus.attr("style", "width:10em; height:2em;");
                    jobStatus.html("<span>" + data.status + "</span>");
                } else if (data.status === 'ERROR') {
                    jobStatus.attr("class", "label label-danger");
                    jobStatus.attr("style", "width:10em; height:2em;");
                    jobStatus.html("<span>" + data.status + "</span>");
                } else if (data.status === 'DEAD') {
                    jobStatus.attr("class", "label label-warning");
                    jobStatus.attr("style", "width:10em; height:2em; background: rgba(230, 110, 30, 1);");
                    jobStatus.html("<span>" + data.status + "</span>");
                }
                $("#job-stopped").text(formatUTCToLocalDateTime(data.stoppedIso));
                $(".triggerButton").prop('disabled', false);
            }
            $("#job-last-updated").text(formatUTCToLocalDateTime(data.lastUpdatedIso));
        }
    });
}

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