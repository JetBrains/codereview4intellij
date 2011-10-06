<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:date="date"
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
                                font-size: 12pt;
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

                            div.context {
                                background: #FFFFDE;
                            }
                            span.context_line {
                                background-color:EBFFED
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
                <xsl:value-of select="@value" disable-output-escaping="yes"/> <xsl:text> </xsl:text>
             </div>
        </div>
    </xsl:template>


    <xsl:template match="context/Context">
        <pre>
            <div class="context">
                <xsl:call-template name="add_line_number">
                    <xsl:with-param name="list" select="line_before" />
                    <xsl:with-param name="starting_number"><xsl:value-of select="line_before_number"/></xsl:with-param>
                </xsl:call-template>
                <xsl:call-template name="add_line_number">
                    <xsl:with-param name="list" select="line" />
                    <xsl:with-param name="starting_number"><xsl:value-of select="line_number"/></xsl:with-param>
                </xsl:call-template>
                <xsl:call-template name="add_line_number">
                    <xsl:with-param name="list" select="line_after" />
                    <xsl:with-param name="starting_number"><xsl:value-of select="line_after_number"/></xsl:with-param>
                </xsl:call-template>
            </div>
        </pre>
    </xsl:template>

    <xsl:template name="review_item" match="review_item">
         <p>
             <div class="review_item">
                <strong><xsl:value-of select="author"/></strong>
                <xsl:text> at </xsl:text>
                <strong>
                    <xsl:call-template name="date-time">
                        <xsl:with-param name="timestamp"><xsl:value-of select="date"/></xsl:with-param>
                    </xsl:call-template>
                </strong>
                <xsl:text> added: </xsl:text> <br/>
                <xsl:text> </xsl:text><xsl:value-of select="text"/><xsl:text> </xsl:text><br/>
             </div>
         </p>
     </xsl:template>

    <xsl:template name="date-time">
        <xsl:param name="timestamp"/>

        <xsl:variable name="datemonth"
              select="document('file://month.xsl')//date:month" />

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
                 count($datemonth
                       /*[$year-offset>=sum(preceding-sibling::*)][last()]
                       /preceding-sibling::*)"/>
            <xsl:variable name="hours"
                          select="floor($time div 3600)"/>
            <xsl:variable name="min"
                          select="floor($time div 60-$hours*60)"/>
            <xsl:variable name="sec"
                          select="floor($time -$hours*3600-$min*60)"/>
            <xsl:variable name="result" select="
                 concat(
                     format-number($hours,'00'),':',
                     format-number($min,'00'),' ',
                     format-number(
                         $year-offset
                         -sum($datemonth/*[$month>=position()])
                         +(2>$month and (($year mod 4=0 and
                                          $year mod 100!=0) or
                                          $year mod 400=0)),
                         '00'),'.',
                         format-number($month+1,'00'), '.',
                         format-number($year,'0000'), ' '
                         )"/>
            <xsl:value-of select="$result"/>
        </xsl:if>
    </xsl:template>

    <xsl:template name="add_line_number">
        <xsl:param name="list" />
        <xsl:param name="starting_number" />
        <xsl:variable name="newlist" select="concat(normalize-space($list), ' ')" />
        <xsl:variable name="first" select="substring-before($newlist, '&lt;br/&gt;')" />
        <xsl:variable name="remaining" select="substring-after($newlist, '&lt;br/&gt;')" />
           <xsl:value-of select="$starting_number"/> <xsl:text>  </xsl:text> <xsl:value-of select="$first" disable-output-escaping="yes"/> <br/>
        <xsl:if test="$remaining">
            <xsl:call-template name="add_line_number">
                    <xsl:with-param name="list" select="$remaining" />
                    <xsl:with-param name="starting_number" select="$starting_number + 1" />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>