function getLog(logIndex) {
    $.ajax({
        type: "GET",
        url: $('.logWindow').data("job-url"),
        headers: {
            Accept: "application/json; charset=utf-8",
            "Content-Type": "application/json; charset=utf-8"
        },
        data: {},
        dataType: "json",
        error: function (data, status, error) {
            alert(error);
        },
        success: function (data, textStatus, xhr) {
            var newLogIndex = data.messages.length - 1;
            var logWindow = $('.logWindow');

            while (logIndex <= newLogIndex) {
                if (logIndex === 0) {
                    logWindow.text("");
                }
                logWindow.append("<div>" + data.messages[logIndex] + "</div>");
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
                    getLog(newLogIndex)
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
                $("#job-stopped").text(data.stopped);
                $(".triggerButton").prop('disabled', false);
            }
            $("#job-last-updated").text(data.lastUpdated);
        }
    });
}

//Uncheck follow log checkbox if real mouse scrolling detected
$(".logWindow").bind("scroll mousedown DOMMouseScroll mousewheel keyup", function (e) {
    if (e.which > 0 || e.type === "mousedown" || e.type === "mousewheel") {
        $("#follow-log").prop('checked', false);
    }
});

setTimeout(function () {
    getLog(0)
}, 1000);