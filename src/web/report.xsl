<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
                xmlns:date="date"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                extension-element-prefixes="date">
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
        <div class="review">
            <xsl:for-each select="tags">
                <xsl:apply-templates select="tag"/>
            </xsl:for-each>
            <xsl:apply-templates select="context/Context"/>
            <br/>
            <xsl:for-each select="review_items">
                <xsl:apply-templates select="review_item"/>
            </xsl:for-each>
        </div>
    </xsl:template>

    <xsl:template name="tag" match="tag">
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

     <xsl:template name="review_item" match="review_item">
         <p>
             <div class="review_item">
                <strong><xsl:value-of select="author"/></strong>
                <xsl:text> at </xsl:text>
                <strong>
                    <xsl:call-template name="date:date-time">
                        <xsl:with-param name="timestamp"><xsl:value-of select="date"/></xsl:with-param>
                    </xsl:call-template>
                </strong>
                <xsl:text> added: </xsl:text><br/><xsl:text> </xsl:text>
                <xsl:value-of select="text"/><xsl:text> </xsl:text><br/>
             </div>
         </p>
     </xsl:template>

     <date:month>
        <january>31</january>
        <february>28</february>
        <march>31</march>
        <april>30</april>
        <may>31</may>
        <june>30</june>
        <july>31</july>
        <august>31</august>
        <september>30</september>
        <october>31</october>
        <november>30</november>
        <december>31</december>
    </date:month>

    <xsl:variable name="date:month"
              select="document('')//date:month"/>

    <xsl:template name="date:date-time">
        <xsl:param name="timestamp"/>

        <xsl:if test="not(format-number($timestamp,0)='NaN')">
            <xsl:variable name="days"
                          select="$timestamp div (24*3600000)"/>
            <xsl:variable name="time"
                          select="
                 $timestamp div 1000
                -floor($days)*24*3600"/>
            <xsl:variable name="year"
                          select="
                 1970+floor(
                     format-number($days div 365.24,'0.#'))"/>
            <xsl:variable name="year-offset"
                          select="
                 719528-$year*365
                 -floor($year div 4)
                 +floor($year div 100)
                 -floor($year div 400)
                 +floor($days)"/>
            <xsl:variable name="month"
                          select="
                 count($date:month
                       /*[$year-offset>=sum(preceding-sibling::*)][last()]
                       /preceding-sibling::*)"/>
            <xsl:variable name="hours"
                          select="floor($time div 3600)"/>
            <xsl:variable name="min"
                          select="floor($time div 60-$hours*60)"/>
            <xsl:variable name="sec"
                          select="floor($time -$hours*3600-$min*60)"/>
            <xsl:variable name="x" select="
                 concat(
                     format-number($year,'0000'),'-',
                     format-number($month+1,'00'),'-',
                     format-number(
                         $year-offset
                         -sum($date:month/*[$month>=position()])
                         +(2>$month and (($year mod 4=0 and
                                          $year mod 100!=0) or
                                          $year mod 400=0)),
                         '00'),' ',
                     format-number($hours,'00'),':',
                     format-number($min,'00'))"/>
            <xsl:value-of select="$x"/>
        </xsl:if>
    </xsl:template>



</xsl:stylesheet>