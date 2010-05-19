// vi: set ts=3 sw=3:
package org.apache.webdav.lib.properties;

import org.apache.webdav.lib.ResponseEntity;
import org.w3c.dom.Element;


/**
 * <code>DAV:modificationdate</code>
 */
public class ModificationDateProperty extends DateProperty
{

    public static final String TAG_NAME = "modificationdate";
    
    public ModificationDateProperty(ResponseEntity response, Element element)
    {
        super(response, element);
    }

}
