<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">
    <xsl:output method="html" indent="yes" encoding="utf-8"/>

    <xsl:template match="all_reviews">
                <html>
                    <head>
                        <title>
                            <xsl:text>Reviews</xsl:text>
                        </title>
                        <style type="text/css">
                            div.review {
                                border: 1px solid black;
                            }
                            div.tag_wrapper {
                                border-bottom: 1px solid #CBCBCB;
                                border-top: 1px solid #CBCBCB;
                                float: left;
                                line-height: 13px;
                                margin: 1px 5px 1px 1px;
                            }
                            div.tag_text {
                               background-color: lightGray;
                               border-color: gray;
                               color: #333333;
                            }

                            tbody {
                                background: #F5F5DC;
                            }
                            td.line {
                                background: lightBlue;
                            }

                            div.deleted div.review_item {
                                    background: #FFCCCC;
                                 }

                            div.existing div.review_item {
                                    background: #CCFFCC;
                                 }
                        </style>
                    </head>
                    <body>
                            <xsl:apply-templates/>
                    </body>
                </html>
    </xsl:template>

    <xsl:template match="all_reviews/FileReviewsList">


        <div>
            <h2><xsl:value-of select="file"/></h2>
            <xsl:apply-templates select="reviews/review"/>
        </div>
    </xsl:template>

    <xsl:template match="reviews/review[.//deleted='true']">
        <div class="deleted">
            <xsl:call-template name="review"/>
        </div>
    </xsl:template>

    <xsl:template match="reviews/review[.//deleted='false']">
        <div class="existing">
            <xsl:call-template name="review"/>
        </div>
    </xsl:template>

    <xsl:template name='review'>
        <!--<h3><strong><xsl:text>Name of review: </xsl:text> </strong><xsl:value-of select="@name"/></h3>-->
        <div class="review">
            <xsl:apply-templates select="tags/tag"/>
            <xsl:apply-templates select="context/Context"/>
            <br/>
            <xsl:apply-templates select="review_items/review_item"/>
        </div>
    </xsl:template>

    <xsl:template match="tags/tag">
        <div class="tag_wrapper">
            <div class="tag_text">
                <xsl:value-of select="@value"/> <xsl:text> </xsl:text>
             </div>
        </div>
    </xsl:template>


    <xsl:template match="context/Context">
        <pre>
            <div>
                <table align="left" frame="hsides">
                 <tr><td><xsl:value-of select="line_before"/></td></tr>
                 <tr><td class="line"><xsl:value-of select="line"/></td></tr>
                 <tr><td><xsl:value-of select="line_after"/></td></tr>
                </table>
            </div>
        </pre>
    </xsl:template>

     <xsl:template match="review_items/review_item">

         <p>
             <div class="review_item">
                <strong><xsl:value-of select="author"/></strong>
                <xsl:text> at </xsl:text>
                <strong><xsl:value-of select="date"/></strong>
                <xsl:text> wrote: </xsl:text><br/>
                <xsl:value-of select="text"/><br/>
             </div>
         </p>
     </xsl:template>

</xsl:stylesheet>