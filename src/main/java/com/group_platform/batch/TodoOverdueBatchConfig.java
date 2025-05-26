//package com.group_platform.batch;
//
//@Configuration
//@EnableBatchProcessing
//public class TodoOverdueBatchConfig {
//
//    private final JobBuilderFactory jobBuilderFactory;
//    private final StepBuilderFactory stepBuilderFactory;
//    private final EntityManagerFactory entityManagerFactory;
//
//    public TodoOverdueBatchConfig(JobBuilderFactory jobBuilderFactory,
//                                  StepBuilderFactory stepBuilderFactory,
//                                  EntityManagerFactory entityManagerFactory) {
//        this.jobBuilderFactory = jobBuilderFactory;
//        this.stepBuilderFactory = stepBuilderFactory;
//        this.entityManagerFactory = entityManagerFactory;
//    }
//
//    @Bean
//    public JpaPagingItemReader<Todo> todoReader() {
//        JpaPagingItemReader<Todo> reader = new JpaPagingItemReader<>();
//        reader.setEntityManagerFactory(entityManagerFactory);
//        reader.setQueryString("SELECT t FROM Todo t WHERE t.dueDate < CURRENT_DATE AND t.status <> 'COMPLETED' AND t.status <> 'OVERDUE'");
//        reader.setPageSize(100);
//        return reader;
//    }
//
//    @Bean
//    public ItemProcessor<Todo, Todo> todoProcessor() {
//        return todo -> {
//            todo.setStatus("OVERDUE");
//            return todo;
//        };
//    }
//
//    @Bean
//    public JpaItemWriter<Todo> todoWriter() {
//        JpaItemWriter<Todo> writer = new JpaItemWriter<>();
//        writer.setEntityManagerFactory(entityManagerFactory);
//        return writer;
//    }
//
//    @Bean
//    public Step updateTodoStatusStep() {
//        return stepBuilderFactory.get("updateTodoStatusStep")
//                .<Todo, Todo>chunk(100)
//                .reader(todoReader())
//                .processor(todoProcessor())
//                .writer(todoWriter())
//                .build();
//    }
//
//    @Bean
//    public Job updateTodoStatusJob(JobCompletionNotificationListener listener) {
//        return jobBuilderFactory.get("updateTodoStatusJob")
//                .listener(listener)
//                .start(updateTodoStatusStep())
//                .build();
//    }
//}