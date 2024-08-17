function refresh() {
    $.get('/persons', function (persons) {
        var list = '';
        (persons || []).forEach(function (person) {
            list = list
                + '<tr>'
                + '<td>' + person.id + '</td>'
                + '<td>' + person.name + '</td>'
                + '<td><a href="#" onclick="deletePerson(' + person.id + ')">Delete</a></td>'
                + '</tr>'
        });
        if (list.length > 0) {
            list = ''
                + '<table><thead><th>Id</th><th>Name</th><th></th></thead>'
                + list
                + '</table>';
        } else {
            list = "No persons in database"
        }
        $('#all-persons').html(list);
    });
}

function deletePerson(id) {
    $.ajax('/persons/' + id, {method: 'DELETE'}).then(refresh);
}

$(document).ready(function () {

    $('#create-person-button').click(function () {
        var personName = $('#person-name').val();
        $.post({
            url: '/persons',
            contentType: 'application/json',
            data: JSON.stringify({name: personName})
        }).then(refresh);
    });

    refresh();
});