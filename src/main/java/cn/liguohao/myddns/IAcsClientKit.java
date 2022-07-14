package cn.liguohao.myddns;

import static cn.liguohao.myddns.EnvNameConstants.ALIYUN_DNS_ACCESS_EY_SECRET;
import static cn.liguohao.myddns.EnvNameConstants.ALIYUN_DNS_ACCESS_KEY_ID;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.alidns.model.v20150109.AddDomainRecordRequest;
import com.aliyuncs.alidns.model.v20150109.AddDomainRecordResponse;
import com.aliyuncs.alidns.model.v20150109.DescribeSubDomainRecordsRequest;
import com.aliyuncs.alidns.model.v20150109.DescribeSubDomainRecordsResponse;
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


    /**
     * 更新(添加或者修改)子域名记录值
     *
     * @param subDomainPrefix 子域名前缀
     * @param domain          域名主体
     * @param newRecordValue  新的记录值
     */
    public static void updateSubDomainRecord(String subDomainPrefix, String domain,
                                             String newRecordValue) {
        Assert.isNotBlank(subDomainPrefix, domain, newRecordValue);

        String subDomain = subDomainPrefix + "." + domain;

        Optional<DescribeSubDomainRecordsResponse.Record> recordOptional =
            getSubDomainRecord(subDomain);

        if (recordOptional.isPresent()) {
            if (!newRecordValue.equals(recordOptional.get().getValue())) {
                // 修改原有的解析记录
                updateDomainRecord(subDomainPrefix, newRecordValue, "A",
                    recordOptional.get().getRecordId());
                LOGGER.info("update subdomain={}, newValue={}, oldValue={}", subDomain,
                    newRecordValue,
                    recordOptional.get().getValue());
            } else {
                LOGGER.info("do not update, because of it has same record value, subdomain={}, newValue={}, oldValue={}", subDomain,
                    newRecordValue,
                    recordOptional.get().getValue());
            }
        } else {
            // 新增解析记录
            addDomainRecord(subDomainPrefix, domain, "A", newRecordValue);
            LOGGER.info("add subdomain={}, recordValue={}", subDomain, newRecordValue);
        }


    }

    private static void addDomainRecord(String subDomainPrefix, String domain,
                                        String type, String newRecordValue) {
        AddDomainRecordRequest request = new AddDomainRecordRequest();
        request.setDomainName(domain);
        request.setRR(subDomainPrefix);
        request.setType(type);
        request.setValue(newRecordValue);

        try {
            AddDomainRecordResponse response = getAliyunDnsClient().getAcsResponse(request);
            System.out.println(new Gson().toJson(response));
        } catch (ServerException e) {
            e.printStackTrace();
            LOGGER.error("request fail, ", e);
        } catch (ClientException e) {
            String sb = "ErrCode:" + e.getErrCode() + "ErrMsg:" +
                e.getErrMsg() + "RequestId:" + e.getRequestId();

            LOGGER.error("request fail, rsp info is " + sb + ". and exception: ", e);
        }
    }

    private static UpdateDomainRecordResponse updateDomainRecord(String subDomainPrefix,
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
            return getAliyunDnsClient().getAcsResponse(request);
        } catch (ServerException e) {
            e.printStackTrace();
            LOGGER.error("request fail, ", e);
        } catch (ClientException e) {
            String sb = "ErrCode:" + e.getErrCode() + "ErrMsg:" +
                e.getErrMsg() + "RequestId:" + e.getRequestId();

            LOGGER.error("request fail, rsp info is " + sb + ". and exception: ", e);
        }
        return null;
    }

}
