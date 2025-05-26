//package com.group_platform.batch;
//
//@Component
//public class TodoOverdueScheduler {
//
//    private final JobLauncher jobLauncher;
//    private final Job updateTodoStatusJob;
//
//    public TodoOverdueScheduler(JobLauncher jobLauncher, Job updateTodoStatusJob) {
//        this.jobLauncher = jobLauncher;
//        this.updateTodoStatusJob = updateTodoStatusJob;
//    }
//
//    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정 0시 0분 0초
//    public void runBatchJob() throws Exception {
//        JobParameters jobParameters = new JobParametersBuilder()
//                .addLong("time", System.currentTimeMillis()) // 매 실행마다 다른 파라미터로 중복 실행 방지
//                .toJobParameters();
//
//        jobLauncher.run(updateTodoStatusJob, jobParameters);
//    }
//}