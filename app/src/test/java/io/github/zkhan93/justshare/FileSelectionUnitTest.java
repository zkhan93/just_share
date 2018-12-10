package io.github.zkhan93.justshare;

import android.app.Activity;
import android.content.Intent;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.*;
import org.robolectric.Shadows;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(RobolectricTestRunner.class)
public class FileSelectionUnitTest {
    private FileSelectionActivity activity;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.setupActivity(FileSelectionActivity.class);
    }

    @Test
    public void startsActivityForResult() {
        activity.onStart();
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertNotNull(startedIntent);
        assertTrue(startedIntent.getCategories().contains(Intent.CATEGORY_OPENABLE));
        assertEquals(startedIntent.getAction(), Intent.ACTION_OPEN_DOCUMENT);
    }

    @Test
    public void parseSelectedFileResult(){
        Intent intent = new Intent()
        activity.onActivityResult(activity.SELECT_FILES_REQUEST_CODE, Activity.RESULT_CANCELED, null);
    }
}