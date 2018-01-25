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

import facebook4j.FacebookException;
import facebook4j.Game;
import facebook4j.ResponseList;
import facebook4j.conf.Configuration;
import facebook4j.internal.http.HttpResponse;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;

/**
 * @author Ryuji Yamashita - roundrop at gmail.com
 */
/*package*/ final class GameJSONImpl extends CategoryJSONImpl implements Game, java.io.Serializable {
    private static final long serialVersionUID = 6796783238460639667L;

    /*package*/GameJSONImpl(HttpResponse res, Configuration conf) throws FacebookException {
        super(res, conf);
    }

    /*package*/GameJSONImpl(JSONObject json) throws FacebookException {
        super(json);
    }

    /*package*/
    static ResponseList<Game> createGameList(HttpResponse res, Configuration conf) throws FacebookException {
        try {
            if (conf.isJSONStoreEnabled()) {
                DataObjectFactoryUtil.clearThreadLocalMap();
            }
            JSONObject json = res.asJSONObject();
            JSONArray list = json.getJSONArray("data");
            final int size = list.length();
            ResponseList<Game> games = new ResponseListImpl<Game>(size, json);
            for (int i = 0; i < size; i++) {
                JSONObject gameJSONObject = list.getJSONObject(i);
                Game game = new GameJSONImpl(gameJSONObject);
                if (conf.isJSONStoreEnabled()) {
                    DataObjectFactoryUtil.registerJSONObject(game, gameJSONObject);
                }
                games.add(game);
            }
            if (conf.isJSONStoreEnabled()) {
                DataObjectFactoryUtil.registerJSONObject(games, list);
            }
            return games;
        } catch (JSONException jsone) {
            throw new FacebookException(jsone);
        }
    }

    @Override
    public String toString() {
        return "GameJSONImpl [id=" + id + ", name=" + name + ", category="
                + category + ", createdTime=" + createdTime + "]";
    }

}
