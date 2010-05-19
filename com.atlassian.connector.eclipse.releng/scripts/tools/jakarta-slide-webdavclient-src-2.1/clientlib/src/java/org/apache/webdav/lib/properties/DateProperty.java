/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/properties/DateProperty.java,v 1.1.2.1 2004/09/26 14:19:20 luetzkendorf Exp $
 * $Revision: 1.1.2.1 $
 * $Date: 2004/09/26 14:19:20 $
 *
 * ====================================================================
 *
 * Copyright 1999-2002 The Apache Software Foundation 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.webdav.lib.properties;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.webdav.lib.BaseProperty;
import org.apache.webdav.lib.ResponseEntity;
import org.apache.webdav.lib.util.DOMUtils;
import org.w3c.dom.Element;

/**
 * Base for all properties that contain date values.
 */
public abstract class DateProperty extends BaseProperty
{
    
    private static final SimpleDateFormat FORMATS[] = {
        new SimpleDateFormat("EEE, d MMM yyyy kk:mm:ss z", Locale.US),
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
        new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US),
        new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
        new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US),
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.US)
    };

    public DateProperty(ResponseEntity response, Element element) {
        super(response, element);
    }
    
    /**
     * Returns the date value.
     */
    public Date getDate()
    {
        String dateString = DOMUtils.getTextValue(element);
        for(int i = 0; i < FORMATS.length; i++) {
            try {
                return FORMATS[i].parse(dateString);
            } catch (ParseException e) {
                // try next
            }
        }
        return null;
    }
}
