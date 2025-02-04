/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.contrib.disk.buffering.internal.exporters;

import io.opentelemetry.contrib.disk.buffering.StoredBatchExporter;
import io.opentelemetry.contrib.disk.buffering.internal.serialization.serializers.SignalSerializer;
import io.opentelemetry.contrib.disk.buffering.internal.storage.Storage;
import io.opentelemetry.contrib.disk.buffering.internal.storage.responses.ReadableResult;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DiskExporter<EXPORT_DATA> implements StoredBatchExporter {
  private final Storage storage;
  private final SignalSerializer<EXPORT_DATA> serializer;
  private final Function<Collection<EXPORT_DATA>, CompletableResultCode> exportFunction;
  private static final Logger logger = Logger.getLogger(DiskExporter.class.getName());

  DiskExporter(
      SignalSerializer<EXPORT_DATA> serializer,
      Function<Collection<EXPORT_DATA>, CompletableResultCode> exportFunction,
      Storage storage) {
    this.serializer = serializer;
    this.exportFunction = exportFunction;
    this.storage = storage;
  }

  public static <T> DiskExporterBuilder<T> builder() {
    return new DiskExporterBuilder<T>();
  }

  @Override
  public boolean exportStoredBatch(long timeout, TimeUnit unit) throws IOException {
    logger.log(Level.INFO, "Attempting to export batch from disk.");
    ReadableResult result =
        storage.readAndProcess(
            bytes -> {
              logger.log(Level.INFO, "About to export stored batch.");
              CompletableResultCode join =
                  exportFunction.apply(serializer.deserialize(bytes)).join(timeout, unit);
              return join.isSuccess();
            });
    return result == ReadableResult.SUCCEEDED;
  }

  public void onShutDown() throws IOException {
    storage.close();
  }

  public CompletableResultCode onExport(Collection<EXPORT_DATA> data) {
    logger.log(Level.FINER, "Intercepting exporter batch.");
    try {
      if (storage.write(serializer.serialize(data))) {
        return CompletableResultCode.ofSuccess();
      } else {
        logger.log(Level.INFO, "Could not store batch in disk. Exporting it right away.");
        return exportFunction.apply(data);
      }
    } catch (IOException e) {
      logger.log(
          Level.WARNING,
          "An unexpected error happened while attempting to write the data in disk. Exporting it right away.",
          e);
      return exportFunction.apply(data);
    }
  }
}
