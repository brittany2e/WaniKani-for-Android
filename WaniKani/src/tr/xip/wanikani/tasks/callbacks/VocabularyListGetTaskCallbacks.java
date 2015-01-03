package tr.xip.wanikani.tasks.callbacks;

import java.util.List;

import tr.xip.wanikani.api.response.VocabularyItem;

/**
 * Created by Hikari on 1/3/15.
 */
public interface VocabularyListGetTaskCallbacks {
    public void onVocabularyListGetTaskPreExecute();

    public void onVocabularyListGetTaskPostExecute(List<VocabularyItem> list);
}
