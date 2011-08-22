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
                        <link rel="stylesheet" type="text/css" href="reviews.css" />
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

    <xsl:template match="reviews/review">
        <div>
          <h3><strong><xsl:text>Name of review: </xsl:text> </strong><xsl:value-of select="@name"/></h3>
            <xsl:apply-templates select="context/Context"/>
            <br/>
            <xsl:apply-templates select="review_items/review_item"/>
        </div>
       </xsl:template>

    <xsl:template match="context/Context">
        <pre>
            <div>
                <table align="left" frame="hsides">
                 <tr><th>Line before</th><td><xsl:value-of select="line_before"/></td></tr>
                 <tr class="line"><th>Line</th><td><xsl:value-of select="line"/></td></tr>
                 <tr><th>Line after</th><td><xsl:value-of select="line_after"/></td></tr>
                </table>
            </div>
        </pre>
    </xsl:template>

     <xsl:template match="review_items/review_item">
         <p>
             <div>
                <strong><xsl:value-of select="author"/></strong>
                <xsl:text> at </xsl:text>
                <strong><xsl:value-of select="date"/></strong>
                <xsl:text> wrote: </xsl:text><br/>
                <xsl:value-of select="text"/><br/>
             </div>
         </p>
     </xsl:template>

</xsl:stylesheet>

