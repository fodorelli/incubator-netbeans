/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.tasks;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;
import org.netbeans.modules.bugtracking.tasks.dashboard.DashboardViewer;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 *
 * @author jpeska
 */
public class NotificationManager {

    private static final ImageIcon DASHBOARD_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/bugtracking/tasks/resources/dashboard.png", true);
    private static NotificationManager instance;
    private final TaskSchedulingManager schedulingManager;
    private ScheduleListener scheduleListener;
    private final DashboardViewer dashboardViewer;
    private Notification scheduleNotification = null;
    private final RequestProcessor rp = new RequestProcessor("Tasks Dashboard - Notifications"); // NOI18N
    private List<IssueImpl> oldTasks = Collections.emptyList();
    private Calendar lastNotification;

    private NotificationManager() {
        this.schedulingManager = TaskSchedulingManager.getInstance();
        this.dashboardViewer = DashboardViewer.getInstance();
    }

    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    public void showNotifications() {
        updateSchedule.schedule(1000);
    }

    private void createScheduleNotification() {
        if (scheduleNotification != null) {
            scheduleNotification.clear();
        }
        removeListener();
        IssueScheduleInfo todayInfo = DashboardUtils.getToday();
        IssueImpl[] scheduledTasks = schedulingManager.getScheduledTasks(todayInfo, dashboardViewer.getRepositories(true).toArray(new RepositoryImpl[0]));
        addListener();
        List<IssueImpl> tasks = Arrays.asList(scheduledTasks);
        if (!scheduleChanged(oldTasks, tasks) && isNotFirstToday() || tasks.isEmpty()) {
            return;
        }

        NotificationDisplayer.Priority priority = NotificationDisplayer.Priority.NORMAL;
        if (isNotFirstToday()) {
            priority = NotificationDisplayer.Priority.SILENT;
        }
        oldTasks = tasks;
        lastNotification = Calendar.getInstance();
        TaskNotificationPanel bubblePanel = new TaskNotificationPanel(tasks, new SelectTodayCategory());
        TaskNotificationPanel notificationPanel = new TaskNotificationPanel(tasks, new SelectTodayCategory());
        String title = NbBundle.getMessage(NotificationManager.class, "LBL_ScheduleTitle", tasks.size());
        scheduleNotification = NotificationDisplayer.getDefault().notify(
                title, DASHBOARD_ICON, bubblePanel, notificationPanel, priority, NotificationDisplayer.Category.INFO
        );
        updateSchedule.schedule(DashboardUtils.getMillisToTomorrow());
    }

    private void addListener() {
        if (scheduleListener == null) {
            scheduleListener = new ScheduleListener();
        }
        schedulingManager.addPropertyChangeListener(scheduleListener);
    }

    private void removeListener() {
        if (scheduleListener != null) {
            schedulingManager.removePropertyChangeListener(scheduleListener);
        }
    }

    private boolean scheduleChanged(List<IssueImpl> oldTasks, List<IssueImpl> newTasks) {
        if (oldTasks.size() != newTasks.size()) {
            return true;
        }
        for (IssueImpl newTask : newTasks) {
            if (!oldTasks.contains(newTask)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNotFirstToday() {
        Calendar todayCalendar = DashboardUtils.getTodayCalendar();
        return lastNotification != null
                && lastNotification.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR)
                && lastNotification.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR);
    }

    private class SelectTodayCategory extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            TopComponent tc = DashboardTopComponent.findInstance();
            tc.open();
            tc.requestActive();
            dashboardViewer.showTodayCategory();
        }
    }

    private final RequestProcessor.Task updateSchedule = rp.create(new Runnable() {
        @Override
        public void run() {
            createScheduleNotification();
        }
    });

    private class ScheduleListener implements PropertyChangeListener {

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(TaskSchedulingManager.PROPERTY_SCHEDULED_TASKS_CHANGED)) {
                updateSchedule.schedule(1000);
            }
        }
    }
}
