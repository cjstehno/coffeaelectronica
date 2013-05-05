$(function(){
    $.ajax('/feeds/recent.xml')
        .done(function( data ){
            var posts = $('#content-area');

            $('entry', data).each(function(idx,itm){
                var tags = _.map($('category',itm), function(cat){
                    return '<span class="label label-info">' + $(cat).attr('term') + '</span>';
                });

                var postSummary = templates['post']({
                    title:$('title',itm).text(),
                    url:$('local-link',itm).text(),
                    blurb:$('summary',itm).text(),
                    date:$('published',itm).text(),
                    tags:tags.join(', ')
                });

                posts.append(postSummary);
            });
        })
        .fail(function(xhr,status, err){
            console.error('Unable to process data feed: ' + err);
        });
});

