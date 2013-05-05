var templates = {};

$(function(){
    _.templateSettings = {
        interpolate : /\{\{(.+?)\}\}/g
    };

    $('.template').each(function(idx,it){
        var templateName = $(it).attr('data-template-name');
        $(it).detach();

        templates[templateName] = _.template($(it).html());
    });
});
