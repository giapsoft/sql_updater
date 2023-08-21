package service;

import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.*;

import java.io.ByteArrayOutputStream;
import java.io.File;

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

    public static boolean isLatest() {
        return true;
    }

    public static void revert() {
    }

    public static long getWorkingRevision(String filePath) throws SVNException {
        return clientManager.getWCClient().doInfo(new File(filePath), SVNRevision.WORKING).getRevision().getNumber();
    }


    public static long update(String filePath) throws SVNException {
        SVNUpdateClient updateClient = clientManager.getUpdateClient();
        updateClient.setIgnoreExternals(false);
        return updateClient.doUpdate(new File(filePath), SVNRevision.HEAD, SVNDepth.INFINITY, true, true);
    }


    public static long updateToRevision(String filePath, long rev) throws SVNException {
        SVNUpdateClient updateClient = clientManager.getUpdateClient();
        updateClient.setIgnoreExternals(false);
        return updateClient.doUpdate(new File(filePath), SVNRevision.create(rev), SVNDepth.INFINITY, true, true);
    }


    public static SVNCommitInfo commit(String path) throws SVNException {
        clientManager.getWCClient().doAdd(new File(Config.svnDir), true, true, true, SVNDepth.INFINITY, false, true);
        SVNCommitPacket m = clientManager.getCommitClient().doCollectCommitItems(new File[]{new File(Config.svnDir)}, false, true, SVNDepth.INFINITY, new String[]{});
        if (m.getCommitItems().length > 0) {
            return clientManager.getCommitClient().doCommit(m, false, "testing commit 2");
        }
        return null;
    }

    public static void main(String[] args) throws SVNException {
        Object printObj = updateToRevision(Config.svnDir, 65422);
        System.out.println(printObj);
    }
}
