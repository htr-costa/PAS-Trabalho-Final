package main.java.com.pasfinal.deliveryservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String DELIVERY_EXCHANGE = "telepizza.delivery.exchange";
    
    private static final String QUEUE_NAME = "telepizza.delivery.queue.instance-" + 
                                             (int)(Math.random() * 10000);

    @Bean
    public String getQueueName() {
        return QUEUE_NAME;
    }

    @Bean
    public Queue deliveryQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .build();
    }

    @Bean
    public TopicExchange deliveryExchange() {
        return new TopicExchange(DELIVERY_EXCHANGE);
    }

    @Bean
    public Binding deliveryBinding(Queue deliveryQueue, TopicExchange deliveryExchange) {

        return BindingBuilder
                .bind(deliveryQueue)
                .to(deliveryExchange)
                .with("delivery.request");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
