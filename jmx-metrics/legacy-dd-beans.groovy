// This is if we decide to add support for older versions of mbeans. These will need to be added to
// kafka.groovy, kafka-producer.groovy and kafka-consumer.groovy if we choose to support.

// kafka.server
def purgatorySize = otel.mbean("kafka.server:type=ProducerRequestPurgatory,name=PurgatorySize")
otel.instrument(purgatorySize, "kafka.request.producer_request_purgatory.size", "decription",
        "1", "Value", otel.&longValueCallback)

def purgatorySizeFetch = otel.mbean("kafka.server:type=FetchRequestPurgatory,name=PurgatorySize")
otel.instrument(purgatorySizeFetch, "kafka.request.fetch_request_purgatory.size", "decription",
        "1", "Value", otel.&longValueCallback)

// kafka.producer
def producerRequestRateAndTimeMs = otel.mbean("kafka.producer:type=ProducerRequestMetrics,name=ProducerRequestRateAndTimeMs,clientId=.*")
otel.instrument(producerRequestRateAndTimeMs, "kafka.producer.request_rate", "decription",
        "1", "Count", otel.&longValueCallback)

def bytesPerSec = otel.mbean("kafka.producer:type=ProducerTopicMetrics,name=BytesPerSec,clientId=.*")
otel.instrument(bytesPerSec, "kafka.producer.bytes_out", "decription",
        "1", "Count", otel.&longValueCallback)

// kafka.consumer
def minFetchRate = otel.mbean("kafka.consumer:type=ConsumerFetcherManager,name=MinFetchRate,clientId=.*")
otel.instrument(minFetchRate, "kafka.consumer.fetch_rate", "decription",
        "1", "value", otel.&longValueCallback)

def bytesPerSecConsumer = otel.mbean("kafka.consumer:type=ConsumerTopicMetrics,name=BytesPerSec,clientId=.*")
otel.instrument(bytesPerSecConsumer, "kafka.consumer.bytes_in", "decription",
        "1", "Count", otel.&longValueCallback)

def messagesPerSec = otel.mbean("kafka.consumer:type=ConsumerTopicMetrics,name=MessagesPerSec,clientId=.*")
otel.instrument(messagesPerSec, "kafka.consumer.messages_in", "decription",
        "1", "Count", otel.&longValueCallback)

// java.lang => Uses Old GC Metrics
