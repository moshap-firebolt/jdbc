package io.firebolt.jdbc.service;

import com.google.common.collect.ImmutableMap;
import io.firebolt.jdbc.PropertyUtil;
import io.firebolt.jdbc.client.query.StatementClient;
import io.firebolt.jdbc.connection.settings.FireboltProperties;
import io.firebolt.jdbc.connection.settings.FireboltQueryParameterKey;
import io.firebolt.jdbc.exception.FireboltException;
import io.firebolt.jdbc.statement.StatementInfoWrapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class FireboltStatementService {

  private final StatementClient statementClient;

  private static final String TAB_SEPARATED_WITH_NAMES_AND_TYPES_FORMAT =
      "TabSeparatedWithNamesAndTypes";

  public InputStream execute(
      @NonNull StatementInfoWrapper statementInfoWrapper,
      @NonNull FireboltProperties connectionProperties,
      Map<String, String> statementParams)
      throws FireboltException {
    Map<String, String> params =
        getAllParameters(connectionProperties, statementInfoWrapper, statementParams);
    return statementClient.postSqlStatement(statementInfoWrapper, connectionProperties, params);
  }

  public void abortStatement(@NonNull String statementId, @NonNull FireboltProperties properties)
      throws FireboltException {
    statementClient.abortStatement(statementId, properties, getCancelParameters(statementId));
  }

  public void abortStatementHttpRequest(@NonNull String statementId) throws FireboltException {
    statementClient.abortRunningHttpRequest(statementId);
  }

  private Map<String, String> getCancelParameters(String statementId) {
    return ImmutableMap.of(FireboltQueryParameterKey.QUERY_ID.getKey(), statementId);
  }

  private Map<String, String> getAllParameters(
      FireboltProperties fireboltProperties,
      StatementInfoWrapper statementInfoWrapper,
      Map<String, String> statementParams) {
    boolean isLocalDb = PropertyUtil.isLocalDb(fireboltProperties);

    Map<String, String> params = new HashMap<>(fireboltProperties.getAdditionalProperties());

    getResponseFormatParameter(
            statementInfoWrapper.getType() == StatementInfoWrapper.StatementType.QUERY, isLocalDb)
        .ifPresent(format -> params.put(format.getLeft(), format.getRight()));

    params.put(FireboltQueryParameterKey.DATABASE.getKey(), fireboltProperties.getDatabase());
    params.put(FireboltQueryParameterKey.QUERY_ID.getKey(), statementInfoWrapper.getId());
    params.put(
        FireboltQueryParameterKey.COMPRESS.getKey(),
        String.format("%d", fireboltProperties.isCompress() ? 1 : 0));
    Optional.ofNullable(statementParams).ifPresent(params::putAll);
    return params;
  }

  private Optional<Pair<String, String>> getResponseFormatParameter(
      boolean isQuery, boolean isLocalDb) {
    if (isQuery) {
      if (isLocalDb) {
        return Optional.of(
            new ImmutablePair<>(
                FireboltQueryParameterKey.DEFAULT_FORMAT.getKey(),
                TAB_SEPARATED_WITH_NAMES_AND_TYPES_FORMAT));
      } else {
        return Optional.of(
            new ImmutablePair<>(
                FireboltQueryParameterKey.OUTPUT_FORMAT.getKey(),
                TAB_SEPARATED_WITH_NAMES_AND_TYPES_FORMAT));
      }
    }
    return Optional.empty();
  }
}
