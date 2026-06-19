import {formatUTCToLocalTime, formatUTCToLocalDateTime, formatInitialDates} from './datetime.js'

let followLog = true;

export function handleScrollEvent(el, btn) {
    const atBottom = el.scrollHeight - el.scrollTop <= el.clientHeight + 5;
    followLog = atBottom;

    if (atBottom) {
        btn.setAttribute("hidden", "hidden");
    } else {
        const scrollbarWidth = el.offsetWidth - el.clientWidth;
        btn.style.right = (scrollbarWidth + 12) + 'px';
        btn.removeAttribute("hidden");
    }
}

export function handleScrollToBottomClick(el, btn) {
    el.scrollTop = el.scrollHeight;
    followLog = true;
    btn.setAttribute("hidden", "hidden");
}

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
            const jobStatus = $('#job-status');
            jobStatus.attr("class", "badge bg-danger");
            jobStatus.attr("style", "width:10em; height:2em;");
            jobStatus.html("<span>UNKNOWN</span>");
        },
        success: function (data, textStatus, xhr) {
            const numberOfMessages = data.messages.length;
            const logWindow = $('.logWindow');

            while (logIndex < numberOfMessages) {
                if (logIndex === 0) {
                    logWindow.empty();
                }
                const msg = data.rawMessages[logIndex];
                const div = document.createElement("div");
                const tsSpan = document.createElement("span");
                tsSpan.textContent = "[" + formatUTCToLocalTime(msg.timestampUTCIsoString) + "]";
                const msgSpan = document.createElement("span");
                msgSpan.textContent = "[" + msg.level + "] " + msg.message;
                div.appendChild(tsSpan);
                div.appendChild(document.createTextNode(" "));
                div.appendChild(msgSpan);
                logWindow[0].appendChild(div);
                logIndex++;
            }

            if (followLog) {
                logWindow.each(function () {
                    const scrollHeight = Math.max(this.scrollHeight, this.clientHeight);
                    this.scrollTop = scrollHeight - this.clientHeight;
                });
            }

            //Schedule further polling if still runnin'
            if (data.state === 'Running') {
                setTimeout(function () {
                    getLog(logIndex)
                }, 2000);
            } else {
                const jobStatus = $('#job-status');
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
    $(".logWindow").bind("scroll mousedown DOMMouseScroll mousewheel keyup", function (e) {
        if (e.which > 0 || e.type === "mousedown" || e.type === "mousewheel" || e.type === "scroll") {
            handleScrollEvent(this, $('#scroll-to-bottom')[0]);
        }
    });

    $("#scroll-to-bottom").on("click", function () {
        handleScrollToBottomClick($('.logWindow')[0], this);
    });


    formatInitialDates();
    setTimeout(function () {
        getLog(0)
    }, 1000);
}