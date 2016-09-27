package org.jcvi.ometa.engine;

import org.jcvi.ometa.helper.SequenceHelper;
import org.junit.Test;

import java.io.File;

/**
 * User: movence
 * Date: 11/25/14
 * Time: 10:12 PM
 * org.jcvi.ometa.engine
 */
public class BulkLoaderTest {
    BulkLoader loader = new BulkLoader();

    @Test
    public void main() throws Exception {
        loader.main(null);
        //this.mailBulk();
        //this.mailSequence();
    }

    private void mailBulk() throws Exception {
        loader.sendResultMail(
                "Hyunsoo Kim", "hkim@jcvi.org", "samples.csv", 3, 1, 2,
                "/Users/movence/works/test/ometa/loader_app/test/log/Log-samples.log",
                "/Users/movence/works/test/ometa/loader_app/test/log/Failed-samples.csv");
    }

    private void mailSequence() throws Exception {
        SequenceHelper helper = new SequenceHelper();
        helper.sendResultMail(
                "Hyunsoo Kim", "hkim@jcvi.org", new File("/Users/movence/works/test/ometa/loader_app/dpcc/hkim.csv"), "data.fasta", "test/data/fasta", "test");
    }
}
