package app;

import org.tmatesoft.svn.core.SVNException;
import service.Config;
import service.Svn;
import working.WorkingAction;

public class App {
    public void init() throws SVNException {
        Svn.update(Config.svnDir);
    }

    public static void commit() {
        WorkingAction.get.commit();
    }

}
