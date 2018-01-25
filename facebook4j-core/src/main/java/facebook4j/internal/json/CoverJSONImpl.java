/*
 * Copyright 2012 Ryuji Yamashita
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package facebook4j.internal.json;

import facebook4j.Cover;
import facebook4j.FacebookException;
import facebook4j.internal.org.json.JSONObject;

import static facebook4j.internal.util.z_F4JInternalParseUtil.*;

/**
 * @author Ryuji Yamashita - roundrop at gmail.com
 */
/*package*/ final class CoverJSONImpl implements Cover, java.io.Serializable {
    private static final long serialVersionUID = 140769718939464754L;
    
    private final String id;
    private final String source;
    private final long offsetY;

    /*package*/CoverJSONImpl(JSONObject json) throws FacebookException {
        id = getRawString("id", json);
        source = getRawString("source", json);
        offsetY = getLong("offset_y", json);
    }

    public String getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public long getOffsetY() {
        return offsetY;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CoverJSONImpl other = (CoverJSONImpl) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CoverJSONImpl [id=" + id + ", source=" + source + ", offsetY="
                + offsetY + "]";
    }

}
