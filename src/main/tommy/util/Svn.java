package util;

import setup.Config;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.*;
import org.tmatesoft.svn.core.wc2.SvnLog;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnRevisionRange;
import org.tmatesoft.svn.core.wc2.SvnTarget;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Svn {
    private static final SVNClientManager clientManager = createSVNClientManager();

    public static SVNClientManager createSVNClientManager() {

        DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
        return SVNClientManager.newInstance(options);
    }

    public static String diff(String filePath, long revFrom, long revTo) throws SVNException {
        return diff(filePath, SVNRevision.create(revFrom), SVNRevision.create(revTo));
    }

    public static String diff(String filePath, SVNRevision from, SVNRevision to) throws SVNException {
        SVNDiffClient diffClient = clientManager.getDiffClient();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        diffClient.doDiff(
                new File(filePath), from,
                new File(filePath), to,
                SVNDepth.INFINITY, false, bos, null);
        return bos.toString();
    }

    public static String currentDiffToHead(String filePath) throws SVNException {
        return diff(filePath, SVNRevision.WORKING, SVNRevision.HEAD);
    }

    public static long getWorkingRevision(String filePath) throws SVNException {
        return clientManager.getWCClient().doInfo(new File(filePath), SVNRevision.WORKING).getRevision().getNumber();
    }


    public static long update(String filePath) throws SVNException {
        SVNUpdateClient updateClient = clientManager.getUpdateClient();
        updateClient.setIgnoreExternals(false);
        return updateClient.doUpdate(new File(filePath), SVNRevision.HEAD, SVNDepth.INFINITY, true, true);
    }

    static void test() throws SVNException {
        int latestRev;
        String dirPath = "", url = "";
        cloneOrUpdate(dirPath, url);
        findRevisions(dirPath);
    }

    public static long updateToRevision(String filePath, long rev) throws SVNException {
        clientManager.getWCClient().doCleanup(new File(Config.get.svnDir), true);
        SVNUpdateClient updateClient = clientManager.getUpdateClient();

        updateClient.setIgnoreExternals(false);
        return updateClient.doUpdate(new File(filePath), SVNRevision.create(rev), SVNDepth.INFINITY, true, true);
    }

    public static void cloneOrUpdate(String dirPath, String url) throws SVNException {
        clientManager.getWCClient().doCleanup(new File(Config.get.svnDir), true);
        SVNUpdateClient updateClient = clientManager.getUpdateClient();
        updateClient.setIgnoreExternals(false);
        updateClient.doUpdate(new File(dirPath), SVNRevision.HEAD, SVNDepth.INFINITY, true, true);
    }


    public static SVNCommitInfo commit(String path, String message) throws SVNException {
        clientManager.getWCClient().doAdd(new File(path), true, true, true, SVNDepth.INFINITY, false, true);
        SVNCommitPacket m = clientManager.getCommitClient().doCollectCommitItems(new File[]{new File(path)}, false, true, SVNDepth.INFINITY, new String[]{});
        if (m.getCommitItems().length > 0) {
            return clientManager.getCommitClient().doCommit(m, false, message);
        }
        return null;
    }

    public static Collection<SVNLogEntry> findRevisions(String filePath) {
        try {
            File file = new File(filePath);
            SvnOperationFactory operationFactory = new SvnOperationFactory();
            SvnLog logOperation = operationFactory.createLog();
            logOperation.setSingleTarget(
                    SvnTarget.fromFile(file)
            );
            logOperation.setRevisionRanges(Collections.singleton(
                    SvnRevisionRange.create(
                            SVNRevision.create(1),
                            SVNRevision.HEAD
                    )
            ));
            return logOperation.run(null);
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    public static Collection<SVNLogEntry> findRevisions() throws SVNException {
        return findRevisions(Config.get.svnDir);
    }

    public static long lastRev() throws SVNException {
        return clientManager.getStatusClient().doStatus(new File(Config.get.svnDir),true).getCommittedRevision().getNumber();
    }
}
