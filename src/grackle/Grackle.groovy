package grackle
import groovy.io.FileType
import groovy.text.GStringTemplateEngine
import groovy.text.TemplateEngine
import groovy.xml.XmlUtil
import org.xml.sax.EntityResolver
import org.xml.sax.InputSource
import org.xml.sax.SAXException

class Grackle {
    // FIXME: might be interesting to have an xml/html compressor

    File sourceDir
    String defaultLayout = 'default'
    String layoutDirName = 'layouts'
    String postDirName = 'posts'
    String pageDirName = 'pages'
    String fileDirName = 'files'

    private static final String POST_DATE_DIR_FORMAT = 'yyyy/MM'
    private static final String PUB_DATE_FORMAT = 'M/d/yyyy'
    private TemplateEngine templateEngine
    private XmlSlurper slurper
    private final templates = [:]

    static void main( final String[] args ){
        def grackle = new Grackle( sourceDir:new File(args[0]) )
        grackle.init()
        grackle.process(new File(args[1]))
    }

    void init(){
//        def parser = SAXParserFactory.newInstance().newSAXParser();
//        parser.getXMLReader().setEntityResolver(new GrackleEntityResolver())
        slurper = new XmlSlurper()

        templateEngine = new GStringTemplateEngine()

        new File( sourceDir, layoutDirName ).eachFile(FileType.FILES){ layoutFile->
            templates[layoutFile.name - '.html'] = templateEngine.createTemplate(layoutFile)
        }
    }

    void process( final File destDir ){
        destDir.mkdirs()

        copyFiles destDir

        generateFeeds( destDir, processPosts(destDir) )
        processPages(destDir)
    }

    private void copyFiles( final File destDir ){
        def rcDir = new File(destDir, 'rc')
        rcDir.mkdirs()

        new File( sourceDir, fileDirName).eachFile(FileType.FILES){ file->
            new File(rcDir, file.name).bytes = file.bytes
        }

        println "Copied static files..."
    }

    private void generateFeeds( final File destDir, content ){
        def posts = content.findAll { it.dirName == postDirName }

        def feedMeta = [ updated:new Date() /* FIXME: need to covnert */ ]
        def feedTemplate = templates['feed.xml']

        def feedsDir = new File(destDir, 'feeds')
        feedsDir.mkdirs()

        // last 10 posts
        new File( feedsDir, 'recent.xml').text = feedTemplate.make([
            feed:feedMeta,
            entries:posts.sort { it.datePublished }.reverse()[0..Math.min(10,posts.size()-1)]
        ])
        println "Generated recent feed..."

        // archive feeds
        posts.groupBy { it.datePublished.format('yyyy-MM') }.each { archDate, archPosts->
            new File( feedsDir, "archives-${archDate}.xml").text = feedTemplate.make([
                feed:feedMeta,
                entries:archPosts
            ])
        }

        println "Generated archive feeds..."

        // tag feeds
        def postsByTag = [:]
        posts.each { post->
            post.tags.each { tag->
                tag = tag.toLowerCase()
                def pbt = postsByTag[tag]
                if ( !pbt ){
                    postsByTag[tag] = ([post] as Set)
                } else {
                    pbt << post
                }
            }
        }

        // FIXME: need to ensure no duplicates in posts ...

        postsByTag.each { tag, taggedPosts->
            new File( feedsDir, "tags-${tag.toLowerCase()}.xml").text = feedTemplate.make([
                feed:feedMeta,
                entries:taggedPosts
            ])
        }

        println "Generated tag feeds..."
    }

    private processPages(final File destDir) {
        processContent(pageDirName){ contentModel, mergedContent ->
            println "Processed: ${contentModel.fileName}..."
            new File(destDir, contentModel.fileName as String).text = mergedContent
        }
    }

    private processPosts(final File destDir){
        processContent(postDirName){ contentModel, mergedContent ->
            def dateDir = "posts/${contentModel.datePublished.format(POST_DATE_DIR_FORMAT)}"
            def outputDir = new File(destDir, dateDir)
            outputDir.mkdirs()

            contentModel.path = "$dateDir/${contentModel.fileName}"

            println "Processed: ${contentModel.fileName}..."

            new File(outputDir, contentModel.fileName as String).text = mergedContent
        }
    }

    private processContent( final String contentDirName, Closure outputWriter ){
        def content = []

        new File(sourceDir, contentDirName).eachFileRecurse(FileType.FILES) { file ->
            def html = slurper.parse(file)

            def contentModel = [
                dirName:contentDirName,
                fileName: file.name,
                path: file.name,
                title: html.head.title.text(),
                summary: findProperty(html, 'summary'),
                tags: findProperty(html,'tags')?.split(','),
                datePublished: convertDate(findProperty(html, 'date-published')),
                scripts: findProperty(html, 'scripts')?.split(',')
            ]

            // 2004-02-11T06:00:00.000-06:00

            outputWriter(
                contentModel,
                templates[findProperty(html, 'layout', defaultLayout)].make(
                    contentModel + [content: XmlUtil.serialize(html.body)[44..-10]]
                )
            )

            content << contentModel
        }

        return content
    }

    // 2004-02-11T06:00:00.000-06:00

    private static Date convertDate( String str ){
        if( str.contains('T') ){
            return Date.parse('yyyy-MM-dd', str.split('T')[0])
        } else {
            return Date.parse(PUB_DATE_FORMAT, str)
        }
    }

    private static String findProperty( def html, String name, String defaultValue = null ){
        html.head.meta.find { elt-> elt.@name.text() == "grackle.$name" }?.@content?.text() ?: defaultValue
    }
}

class GrackleEntityResolver implements EntityResolver {

    @Override
    InputSource resolveEntity(final String s, final String s1) throws SAXException, IOException {
        println "$s : $s1"
    }
}