package com.yunliantao.qkl.utils;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.NewAccountIdentifier;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.geth.Geth;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ETHCoinUtils {
    private static final String URL = "http://47.57.64.178:15937/";
    //    private static final String URL = "http://35.236.188.224:55555/";
    /*中央归集地址*/
    public static final String MAINADDRESS = "0xd93d857009dab832b1f269111654483f71b9e442";

    public static final String FREEADDRESS = "0xe4a77b19a17c362282ac9fca671e952bbb9ba38a";

    public static Web3j initWeb3j() {
        return Web3j.build(getService());
    }

    /**
     * 初始化personal级别的操作对象
     *
     * @return Geth
     */
    public static Geth initGeth() {
        return Geth.build(getService());
    }

    /**
     * 初始化admin级别操作的对象
     *
     * @return Admin
     */
    public static Admin initAdmin() {
        return Admin.build(getService());
    }

    /**
     * 通过http连接到geth节点
     *
     * @return
     */
    private static HttpService getService() {
        return new HttpService(URL);
    }


    /**
     * 新增账户
     *
     * @param password
     * @return
     * @throws IOException
     */
    public static String newAccount(String password) throws IOException {
        Admin admin = initAdmin();

        Request<?, NewAccountIdentifier> request = admin.personalNewAccount(password);
        NewAccountIdentifier result = request.send();

        return result.getAccountId();
    }

    /**
     *@描述  以太坊生成地址
     *@参数 [pwd(地址密码)]
     *@返回值 EthApi
     *@修改人和其它信息
     */
    public  static ETHApi createNewAddress(String pwd){
        try{
            ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            WalletFile walletFile = Wallet.createStandard(pwd, ecKeyPair);
            String keystore = objectMapper.writeValueAsString(walletFile);
            WalletFile walletFile2 = objectMapper.readValue(keystore, WalletFile.class);
            ECKeyPair ecKeyPair1 = Wallet.decrypt(pwd, walletFile2);
            return new ETHApi("0x"+walletFile.getAddress(),ecKeyPair1.getPrivateKey().toString(16)) ;
        }catch (Exception e){
            e.printStackTrace();
            log.info("虚拟币ETH创建地址失败，错误代码：{}",new Object[]{e.getMessage()});
            return null ;
        }
    }

    /**
     * 获取当前区块高度
     *
     * @return
     * @throws IOException
     */
    public static BigInteger getCurrentBlockNumber() throws IOException {
        Web3j web3j = initWeb3j();
        Request<?, EthBlockNumber> request = web3j.ethBlockNumber();
        return request.send().getBlockNumber();
    }

    /**
     * 获取当前区块高度
     *
     * @return
     * @throws IOException
     */
    public static BigInteger getBlock() throws IOException {
        Admin web3j = initAdmin();
        Request<?, EthBlockNumber> request = web3j.ethBlockNumber();
        return request.send().getBlockNumber();
    }

    /**
     * 获取当前块  transactions 为当前块中的交易 List
     *
     * @param blockNumber
     * @return
     * @throws IOException
     */
    public static EthBlock getBlockEthBlock(Integer blockNumber) throws IOException {
        Web3j web3j = initWeb3j();
        DefaultBlockParameter defaultBlockParameter = new DefaultBlockParameterNumber(blockNumber);
        Request<?, EthBlock> request = web3j.ethGetBlockByNumber(defaultBlockParameter, true);
        EthBlock ethBlock = request.send();
        return ethBlock;
    }


    public static EthGetBalance getAddressBlance(String address) throws IOException {
        Admin admin = initAdmin();
        Request<?, EthGetBalance> request = admin.ethGetBalance(address, DefaultBlockParameter.valueOf(getCurrentBlockNumber()));
        EthGetBalance result = request.send();
        return result;
    }

    public static EthGetTransactionReceipt ethGetTransactionReceipt(String hex) throws IOException {
        Web3j web3j = initWeb3j();
        Request<?, EthGetTransactionReceipt> request = web3j.ethGetTransactionReceipt(hex);
        EthGetTransactionReceipt ethTransaction = request.send();
        return ethTransaction;
    }

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ETHCoinUtils.class);

    /**
     * 发起交易
     *
     * @param address
     * @param passwrod
     * @param GAS_PRICE
     * @param GAS_LIMIT
     * @param mainAddress
     * @return
     * @throws IOException
     */
    public static String transion(String address, String passwrod, BigInteger GAS_PRICE, BigInteger GAS_LIMIT, String mainAddress) throws IOException {
        Admin admin = initAdmin();
        EthGetBalance eth = getAddressBlance(address);
        if (eth.getBalance().compareTo(BigInteger.ZERO) > 0) {
            BigInteger xx = eth.getBalance();
            logger.info("归集-----------eth->:" + address + "---------->" + mainAddress + "---------->" + xx);
            PersonalUnlockAccount personalUnlockAccount = admin.personalUnlockAccount(address, passwrod).send();
            System.out.println(personalUnlockAccount.accountUnlocked());
            EthGetTransactionCount ethGetTransactionCount = admin
                    .ethGetTransactionCount(address, DefaultBlockParameterName.PENDING)
                    .send();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            logger.info("eth交易》nonce" + null);
            BigInteger b = xx.subtract((GAS_PRICE).multiply(GAS_LIMIT));
            logger.info("eth交易》交易额：" + b);
            Transaction transaction = Transaction.createEtherTransaction(address,
                    nonce, GAS_PRICE, GAS_LIMIT, mainAddress, b);
            logger.info("eth交易》" + JSON.toJSONString(transaction));
            EthSendTransaction transactionResponse = admin
                    .ethSendTransaction(transaction).send();
            logger.info("eth交易接收数据---------》" + JSON.toJSONString(transactionResponse));
            String transactionHash = transactionResponse.getTransactionHash();
            return transactionHash;

        } else {
            logger.info("eth" + address + "用户连续充值，第一次就已经归集完成");
            return null;
        }
    }

    /*public static void main(String[] args) {
        String hex =  ETHCoinUtils.sendTokenTransaction(
                "0x459bab9219d0647910323a98db95afda21456b28",
                "lisa268",
                "0xab79ee78bA85D7f858a2d9cF5B748654eC8E44C6",
                "0xdac17f958d2ee523a2206206994597c13d831ec7",
                new BigDecimal("381862744.00").toBigInteger());//381.862744         0xdac17f958d2ee523a2206206994597c13d831ec7
        System.out.println(hex);
    }*/


    /**
     *
     * @param fromAddress 用户地址
     * @param password    密码
     * @param toAddress    中央账户地址
     * @param contractAddress  合约地址
     * @param amount   数量
     * @return
     */
    public static String sendTokenTransaction(String fromAddress, String password, String toAddress, String contractAddress, BigInteger amount) {
        String txHash = null;
        logger.info("ERC20代币开始归集-------->" + fromAddress);
        try {
            PersonalUnlockAccount personalUnlockAccount = initAdmin().personalUnlockAccount(
                    fromAddress, password, BigInteger.valueOf(1000)).send();
            if (personalUnlockAccount.accountUnlocked()) {
                String methodName = "transfer";
                List<Type> inputParameters = new ArrayList<>();
                List<TypeReference<?>> outputParameters = new ArrayList<>();

                /*中央账户地址*/
                Address tAddress = new Address(toAddress);

                /*数量*/
                Uint256 value = new Uint256(amount);
                inputParameters.add(tAddress);
                inputParameters.add(value);

                TypeReference<Bool> typeReference = new TypeReference<Bool>() {
                };
                outputParameters.add(typeReference);

                Function function = new Function(methodName, inputParameters, outputParameters);

                String data = FunctionEncoder.encode(function);
                Web3j web3j = initWeb3j();
                EthGetTransactionCount ethGetTransactionCount = web3j
                        .ethGetTransactionCount(fromAddress, DefaultBlockParameterName.PENDING).sendAsync().get();
                BigInteger nonce = ethGetTransactionCount.getTransactionCount();
                BigInteger gasPrice = Convert.toWei(BigDecimal.valueOf(32), Convert.Unit.GWEI).toBigInteger();

                Transaction transaction = Transaction.createFunctionCallTransaction(fromAddress, nonce, gasPrice,
                        BigInteger.valueOf(100000), contractAddress, data);

                EthSendTransaction ethSendTransaction = web3j.ethSendTransaction(transaction).sendAsync().get();
                txHash = ethSendTransaction.getTransactionHash();
                logger.info("ERC20归集txHash" + txHash);
                System.out.println(txHash);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return txHash;
    }


    public static void main(String[] args) {
        BigInteger GAS_PRICE2 = BigInteger.valueOf(106000000000L);

        BigInteger GAS_LIMIT2 = BigInteger.valueOf(22_000_000_000L);
        BigInteger GAS_PRICE3 =BigInteger.valueOf(3L * 10000000000L);
        BigInteger GAS_LIMIT4 =BigInteger.valueOf(21000);
//        BigInteger GAS_LIMIT5 =new BigInteger("0xb9e0c346a6000");
        BigInteger GAS_LIMIT5 =new BigInteger("3900000000000000");
        System.out.println(GAS_PRICE2);
        System.out.println(GAS_LIMIT2);
        System.out.println(GAS_PRICE3);
        System.out.println(GAS_LIMIT4);

        BigInteger amountWei = Convert.toWei("0.001", Convert.Unit.ETHER).toBigInteger();
        Transaction etherTransaction = Transaction.createEtherTransaction(null,
                null, GAS_PRICE2, GAS_LIMIT2, null, amountWei);
        logger.info("eth交易》" + etherTransaction);
        logger.info("eth交易》" + etherTransaction.getValue());
        System.out.println(GAS_LIMIT5);
    }
    /**
     * 转入手续费（代币）
     *
     * @param address
     * @param passwrod
     * @param GAS_PRICE
     * @param GAS_LIMIT
     * @param mainAddress
     * @param value  手续费
     * @return
     * @throws IOException
     */
    public static String transionfree(String address, String passwrod, BigInteger GAS_PRICE, BigInteger GAS_LIMIT, String mainAddress, String value) throws IOException {
        Admin admin = initAdmin();

        logger.info("归集-----------eth->:" + address + "---------->" + mainAddress + "---------->" + value);
        PersonalUnlockAccount personalUnlockAccount = admin.personalUnlockAccount(address, passwrod).send();
        System.out.println(personalUnlockAccount.accountUnlocked());
        EthGetTransactionCount ethGetTransactionCount = admin
                .ethGetTransactionCount(address, DefaultBlockParameterName.LATEST)
                .send();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();
        logger.info("eth交易》nonce" + nonce);
        BigInteger b = (new BigDecimal(value).toBigInteger()).subtract((GAS_PRICE).multiply(GAS_LIMIT));
        logger.info("eth交易》交易额：" + b);


        Transaction transaction = Transaction.createEtherTransaction(address,
                nonce, GAS_PRICE, GAS_LIMIT, mainAddress, b);
        logger.info("eth交易》" + JSON.toJSONString(transaction));
        EthSendTransaction transactionResponse = admin.ethSendTransaction(transaction).send();
        logger.info("eth交易接收数据---------》" + JSON.toJSONString(transactionResponse));
        String transactionHash = transactionResponse.getTransactionHash();
        return transactionHash;
    }

    //获取代币余额
    private static final String DATA_PREFIX = "0x70a08231000000000000000000000000";

    /**
     * 获取代币余额
     *
     * @param address         自己地址
     * @param contractAddress 合约地址
     * @return
     * @throws IOException
     */
    public static BigInteger getBalance(String address, String contractAddress) throws IOException {
        Admin admin = initAdmin();
        String value = admin.ethCall(Transaction.createEthCallTransaction(address,
                contractAddress, DATA_PREFIX + address.substring(2)), DefaultBlockParameterName.PENDING).send().getValue();
        return new BigInteger(value.substring(2), 16);
    }

//    //创建一个地址作为手续费地址
//    public static void main(String[] args) throws IOException {
//        String address = newAccount("ydcpass");
//        System.out.print(address);
//    }
}