package cn.liguohao.myddns;

import static cn.liguohao.myddns.EnvNameConstants.ALIYUN_DNS_ACCESS_EY_SECRET;
import static cn.liguohao.myddns.EnvNameConstants.ALIYUN_DNS_ACCESS_KEY_ID;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.alidns.model.v20150109.AddDomainRecordRequest;
import com.aliyuncs.alidns.model.v20150109.AddDomainRecordResponse;
import com.aliyuncs.alidns.model.v20150109.DeleteSubDomainRecordsRequest;
import com.aliyuncs.alidns.model.v20150109.DeleteSubDomainRecordsResponse;
import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordInfoRequest;
import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordInfoResponse;
import com.aliyuncs.alidns.model.v20150109.DescribeSubDomainRecordsRequest;
import com.aliyuncs.alidns.model.v20150109.DescribeSubDomainRecordsResponse;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordRemarkRequest;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordRemarkResponse;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordRequest;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.profile.DefaultProfile;
import com.google.gson.Gson;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author li-guohao
 * @date 2022/07/14
 */
public class IAcsClientKit {
    private static final Logger LOGGER = LoggerFactory.getLogger(IAcsClientKit.class);

    private static IAcsClient getAliyunDnsClient() {
        String aliyunDnsAccessKeyId = System.getenv(ALIYUN_DNS_ACCESS_KEY_ID);
        String aliyunDnsAccessKeySecret = System.getenv(ALIYUN_DNS_ACCESS_EY_SECRET);

        if (aliyunDnsAccessKeyId == null || "".equals(aliyunDnsAccessKeyId)
            || aliyunDnsAccessKeySecret == null || "".equals(aliyunDnsAccessKeySecret)) {
            throw new IllegalArgumentException(
                "env var not set. ALIYUN_DNS_ACCESS_KEY_ID=[" + aliyunDnsAccessKeyId +
                    "] ALIYUN_DNS_ACCESS_EY_SECRET=[" + aliyunDnsAccessKeySecret + "]");
        }

        DefaultProfile profile = DefaultProfile.getProfile("cn-shanghai", aliyunDnsAccessKeyId,
            aliyunDnsAccessKeySecret);
        return new DefaultAcsClient(profile);
    }

    /**
     * 查询子域名解析值
     *
     * @param subDomain 子域名完整的
     * @return 解析值
     */
    public static Optional<DescribeSubDomainRecordsResponse.Record> getSubDomainRecord(
        String subDomain) {
        Assert.isNotBlank(subDomain);

        DescribeSubDomainRecordsRequest request = new DescribeSubDomainRecordsRequest();
        request.setSubDomain(subDomain);

        try {
            DescribeSubDomainRecordsResponse response =
                getAliyunDnsClient().getAcsResponse(request);

            return response.getDomainRecords().stream().findFirst();
        } catch (ServerException e) {
            e.printStackTrace();
            LOGGER.error("request fail, ", e);
        } catch (ClientException e) {
            String sb = "ErrCode:" + e.getErrCode() + "ErrMsg:" +
                e.getErrMsg() + "RequestId:" + e.getRequestId();

            LOGGER.error("request fail, rsp info is " + sb + ". and exception: ", e);
        }
        return Optional.empty();
    }



    public static void updateSubDomainRecord(String subDomainPrefix, String domain,
                                             String newRecordValue, String remarks) {
        Assert.isNotBlank(subDomainPrefix, domain, newRecordValue);

        String subDomain = subDomainPrefix + "." + domain;

        Optional<DescribeSubDomainRecordsResponse.Record> recordOptional =
            getSubDomainRecord(subDomain);

        String recordId = null;

        if (recordOptional.isPresent()) {
            DescribeSubDomainRecordsResponse.Record record = recordOptional.get();
            if (!newRecordValue.equals(record.getValue())) {
                // 修改原有的解析记录
                Optional<UpdateDomainRecordResponse> updateDomainRecordResponseOptional =
                    updateDomainRecord(subDomainPrefix, newRecordValue, "A",
                        record.getRecordId());

                if (updateDomainRecordResponseOptional.isPresent()) {
                    recordId = updateDomainRecordResponseOptional.get().getRecordId();
                }

                updateRemarkExist(remarks, recordId);
            } else {
                LOGGER.info(
                    "do not update, because of it has same record value, subdomain={}, newValue={}, oldValue={}",
                    subDomain, newRecordValue, record.getValue());
            }
        } else {
            // 新增解析记录
            Optional<AddDomainRecordResponse> addDomainRecordResponseOptional =
                addDomainRecord(subDomainPrefix, domain, "A", newRecordValue);

            if (addDomainRecordResponseOptional.isPresent()) {
                recordId = addDomainRecordResponseOptional.get().getRecordId();
            }

            updateRemarkExist(remarks, recordId);
        }


    }

    private static void updateRemarkExist(String remarks, String recordId) {
        // 经过上面的操作后，域名记录一定是存在的，如果传入的备注不为空，更新备注
        if (remarks != null && !"".equals(remarks)) {
            updateDomainRecordRemarks(recordId, remarks);
        }
    }

    private static Optional<UpdateDomainRecordRemarkResponse> updateDomainRecordRemarks(
        String recordId, String remarks) {
        Assert.isNotBlank(recordId, remarks);

        UpdateDomainRecordRemarkRequest request = new UpdateDomainRecordRemarkRequest();
        request.setRecordId(recordId);
        request.setRemark(remarks);

        try {
            UpdateDomainRecordRemarkResponse response = getAliyunDnsClient().getAcsResponse(request);
            LOGGER.info("update domain record remarks, recordId={}, remarks={}",
                recordId, remarks);
            return Optional.of(response);
        } catch (ServerException e) {
            e.printStackTrace();
            LOGGER.error("request fail, ", e);
        } catch (ClientException e) {
            String sb = "ErrCode:" + e.getErrCode() + "ErrMsg:" +
                e.getErrMsg() + "RequestId:" + e.getRequestId();

            LOGGER.error("request fail, rsp info is " + sb + ". and exception: ", e);
        }
        return Optional.empty();
    }

    public static Optional<AddDomainRecordResponse> addDomainRecord(String subDomainPrefix,
                                                                    String domain,
                                                                    String type,
                                                                    String newRecordValue) {
        AddDomainRecordRequest request = new AddDomainRecordRequest();
        request.setDomainName(domain);
        request.setRR(subDomainPrefix);
        request.setType(type);
        request.setValue(newRecordValue);

        try {
            AddDomainRecordResponse acsResponse = getAliyunDnsClient().getAcsResponse(request);
            LOGGER.info("add new domain record, domain={}, pr={}, type={}, value={}",
                domain, subDomainPrefix, type, newRecordValue);
            return Optional.of(acsResponse);
        } catch (ServerException e) {
            e.printStackTrace();
            LOGGER.error("request fail, ", e);
        } catch (ClientException e) {
            String sb = "ErrCode:" + e.getErrCode() + "ErrMsg:" +
                e.getErrMsg() + "RequestId:" + e.getRequestId();

            LOGGER.error("request fail, rsp info is " + sb + ". and exception: ", e);
        }
        return Optional.empty();
    }

    public static Optional<UpdateDomainRecordResponse> updateDomainRecord(String subDomainPrefix,
                                                                          String newRecordValue,
                                                                          String type,
                                                                          String recordId) {
        Assert.isNotBlank(subDomainPrefix, newRecordValue, type, recordId);

        UpdateDomainRecordRequest request = new UpdateDomainRecordRequest();
        request.setRecordId(recordId);
        request.setRR(subDomainPrefix);
        request.setType(type);
        request.setValue(newRecordValue);

        try {
            UpdateDomainRecordResponse response = getAliyunDnsClient().getAcsResponse(request);
            LOGGER.info("update domain record, recordId={}, pr={}, type={}, value={}",
                recordId, subDomainPrefix, type, newRecordValue);
            return Optional.of(response);
        } catch (ServerException e) {
            e.printStackTrace();
            LOGGER.error("request fail, ", e);
        } catch (ClientException e) {
            String sb = "ErrCode:" + e.getErrCode() + "ErrMsg:" +
                e.getErrMsg() + "RequestId:" + e.getRequestId();

            LOGGER.error("request fail, rsp info is " + sb + ". and exception: ", e);
        }
        return Optional.empty();
    }

    public static Optional<DeleteSubDomainRecordsResponse> deleteDomainARecord(
        String subDomainPrefix, String domain) {
        return deleteDomainRecord(subDomainPrefix, domain, "A");
    }

    public static Optional<DeleteSubDomainRecordsResponse> deleteDomainRecord(
        String subDomainPrefix, String domain, String type) {
        Assert.isNotBlank(subDomainPrefix, domain, type);

        DeleteSubDomainRecordsRequest request = new DeleteSubDomainRecordsRequest();
        request.setDomainName(domain);
        request.setRR(subDomainPrefix);

        try {
            DeleteSubDomainRecordsResponse response = getAliyunDnsClient().getAcsResponse(request);
            LOGGER.info("delete domain record, pr={}, domain={}, type={}",
                subDomainPrefix, domain, type);
            return Optional.of(response);
        } catch (ServerException e) {
            e.printStackTrace();
            LOGGER.error("request fail, ", e);
        } catch (ClientException e) {
            String sb = "ErrCode:" + e.getErrCode() + "ErrMsg:" +
                e.getErrMsg() + "RequestId:" + e.getRequestId();

            LOGGER.error("request fail, rsp info is " + sb + ". and exception: ", e);
        }
        return Optional.empty();
    }

}
