function update() {

    var jobsContainer = $('#jobsContainer');
    var typeFilter = jobsContainer.data("type-filter");
    if (!typeFilter) {
        typeFilter='';
    }
    var jobsUrl = jobsContainer.data("jobs-url");

    $.ajax({
        type: "GET",
        url: jobsUrl + "?humanReadable=true" + (typeFilter === '' ? '' : "&type=" + typeFilter),
        headers: {
            Accept: "application/json",
            "Content-Type": "application/json"
        },
        data: {},
        dataType: "json",
        error: function (data, status, error) {
            console.log("Error polling job status");
            setTimeout(update, 10000);
        },
        success: function (data, textStatus, xhr) {
            for (var i in data) {
                var dataRow = null;
                dataRow = data[i];

                var jobStatus = $('#job-status-' + dataRow.id);
                //There is a new job that is not in this list -> reload page!
                if (!jobStatus.length) {
                    location.reload();
                }

                if (dataRow.state !== 'Running') {
                    if (dataRow.status === 'OK') {
                        jobStatus.attr("class", "label label-success");
                        jobStatus.attr("style", "width:10em; height:2em;");
                        jobStatus.html("<span>" + dataRow.status + "</span>");
                    } else if (dataRow.status === 'SKIPPED') {
                        jobStatus.attr("class", "label label-default");
                        jobStatus.attr("style", "width:10em; height:2em;");
                        jobStatus.html("<span>" + dataRow.status + "</span>");
                    } else if (dataRow.status === 'ERROR') {
                        jobStatus.attr("class", "label label-danger");
                        jobStatus.attr("style", "width:10em; height:2em;");
                        jobStatus.html("<span>" + dataRow.status + "</span>");
                    } else if (dataRow.status === 'DEAD') {
                        jobStatus.attr("class", "label label-warning");
                        jobStatus.attr("style", "width:10em; height:2em; background: rgba(230, 110, 30, 1);");
                        jobStatus.html("<span>" + dataRow.status + "</span>");
                    }
                    $("#trigger-button-" + dataRow.id).prop('disabled', false);
                }
                $("#job-started-" + dataRow.id).text(formatUTCToLocalDateTime(dataRow.startedIso));
                $("#job-stopped-" + dataRow.id).text(formatUTCToLocalDateTime(dataRow.stoppedIso));
                $("#job-runtime-" + dataRow.id).text(dataRow.runtime);
                $("#job-last-updated-" + dataRow.id).text(dataRow.lastUpdated);
            }
            setTimeout(update, 4000);
        }
    });
}

formatInitialDates();
setTimeout(function () {
    update();
}, 1000);