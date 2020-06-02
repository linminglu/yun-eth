package com.yunliantao.qkl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.*;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

//import org.apache.log4j.log;

/**
 * @author yang杭
 * 文本说明：
 * @date 2020/5/27  17:07
 */
@Slf4j
public class ETHApi {

//    private static log log = log.getlog(ETHApi.class) ;
    private String url="";
    private static Web3j web3j ;
    private String createAddress ;
    private String privatekey ;
    public ETHApi(String url) {
        this.url = url;
    }
    public ETHApi(String createAddress, String privatekey) {
        this.createAddress = createAddress;
        this.privatekey = privatekey;
    }
    /**
     *@描述 获取ETH连接，初始化方法
     *@参数 [url, coinDecimal]
     *@返回值 EthApi
     *@修改人和其它信息
     */
    public static ETHApi getInitialization(String url){
        try{
            web3j = Web3j.build(new HttpService(url));
        }catch (Exception e){
            e.printStackTrace();
            log.info("虚拟币ETH获取连接失败，错误代码：{}",new Object[]{e.getMessage()});
        }
        return new ETHApi(url);
    }
    /**
     *@描述  以太坊获取地址余额
     *@参数 [address]
     *@返回值 double
     *@修改人和其它信息
     */
    public double getBalance(String address){
        double balance = 0.0 ;
        try{
            EthGetBalance getBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
            if(!getBalance.hasError()){
                String value = getBalance.getBalance().toString();
                double valueDouble = Convert.fromWei(value, Convert.Unit.ETHER).doubleValue();
                if(valueDouble > 0){
                    return valueDouble;
                }else{
                    return 0;
                }
            }else{
                return 0;
            }
        }catch (Exception e){
            e.printStackTrace();
            log.info("虚拟币ETH获取余额失败，错误代码：{}",new Object[]{e.getMessage()});
            return balance ;
        }
    }
    /**
     *@描述  获取以太坊代币余额
     *@参数 [fromAddress(代币地址), contractAddress(代币合约地址), decimal(小数位数)]
     *@返回值 double
     *@修改人和其它信息
     */
    public double tokenGetBalance(String fromAddress, String contractAddress, int decimal) {
        try
        {
            String methodName = "balanceOf";
            List inputParameters = new ArrayList();
            List outputParameters = new ArrayList();
            Address address = new Address(fromAddress);
            inputParameters.add(address);
            TypeReference typeReference = new TypeReference<Uint256>()
            {
            };
            outputParameters.add(typeReference);
            Function function = new Function(methodName, inputParameters, outputParameters);
            String data = FunctionEncoder.encode(function);
            Transaction transaction = Transaction.createEthCallTransaction(fromAddress, contractAddress, data);

            BigInteger balanceValue = BigInteger.ZERO;
            try {
                EthCall ethCall = (EthCall)this.web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
                List results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
                balanceValue = (BigInteger)((Type)results.get(0)).getValue();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return BigDecimalUtils.divide(new BigDecimal(balanceValue).doubleValue(), Math.pow(10.0D, decimal), 8);
        } catch (Exception e) {
            log.info("虚拟币ETH代币获取余额失败，错误代码：{}",new Object[]{e.getMessage()});
            e.printStackTrace();
        }
        return decimal;
    }



    /**
     *@描述  以太坊生成地址
     *@参数 [pwd(地址密码)]
     *@返回值 EthApi
     *@修改人和其它信息
     */
    public ETHApi createNewAddress(String pwd){
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
     *@描述  ETH 转账
     *@参数 [from(转出地址), to(转入地址), amount(转出金额), privateKey(转出地址私钥)]
     *@返回值 java.lang.String
     *@修改人和其它信息
     */
    public  String signETHTransaction(String from,String to,String amount,String privateKey){
        try{
            BigInteger nonce = web3j.ethGetTransactionCount(from, DefaultBlockParameterName.PENDING).send().getTransactionCount();
            //支付的矿工费
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
            BigInteger gasLimit = new BigInteger("60000");
            BigInteger amountWei = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();
            //签名交易
            RawTransaction rawTransaction = RawTransaction.createTransaction (nonce, gasPrice, gasLimit, to, amountWei, "");
            Credentials credentials = Credentials.create(privateKey);
            byte[] signMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            //广播交易0
            String hash =  web3j.ethSendRawTransaction(Numeric.toHexString(signMessage)).sendAsync().get().getTransactionHash();
            log.info("ETH转账,发送方:{"+from+"},接收方:{"+to+"},发送金额:{"+amount+"},hash:{"+hash+"}",new Object[]{from,to,amount,hash});
            return hash ;
        }catch (Exception e){
            e.printStackTrace();
            log.info("虚拟币ETH转账失败，错误代码：{}",new Object[]{e.getMessage()});
            return null ;
        }
    }
    /**
     * 查询代币精度
     *
     * @param contractAddress
     * @return
     */
    public static int getTokenDecimal(String contractAddress) {
        String methodName = "decimals";
        String fromAddr = "0x0000000000000000000000000000000000000000";
        int decimal = 0;
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();
        TypeReference<Uint8> typeReference = new TypeReference<Uint8>() {
        };
        outputParameters.add(typeReference);
        Function function = new Function(methodName, inputParameters, outputParameters);
        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction(fromAddr, contractAddress, data);
        EthCall ethCall;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).sendAsync().get();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            decimal = Integer.parseInt(results.get(0).getValue().toString());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return decimal;
    }
    /**
     *@描述  eth代币转账
     *@参数 [from(代币转出地址), to(代币转入地质), amount(转出金额), privateKey(转出地址私钥), coinAddress(代币合约地址)]
     *@返回值 java.lang.String
     *@修改人和其它信息
     */
    public String signTokenTransaction(String from,String to,String amount,String privateKey,String coinAddress){
        try{
            BigInteger nonce = web3j.ethGetTransactionCount(from, DefaultBlockParameterName.PENDING).send().getTransactionCount();
            //支付的矿工费
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
            BigInteger gasLimit = new BigInteger("60000");

            Credentials credentials = Credentials.create(privateKey);
            int tokenDecimal = getTokenDecimal(coinAddress);
            BigInteger amountWei = null ;
            if(tokenDecimal == 6){
                amountWei = Convert.toWei(amount, Convert.Unit.MWEI).toBigInteger();
            }else{
                amountWei = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();
            }
            //封装转账交易
            Function function = new Function(
                    "transfer",
                    Arrays.<Type>asList(new Address(to),
                            new Uint256(amountWei)),
                    Collections.<TypeReference<?>>emptyList());
            String data = FunctionEncoder.encode(function);
            //签名交易
            RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, coinAddress, data);
            byte[] signMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            //广播交易
            String hash = web3j.ethSendRawTransaction(Numeric.toHexString(signMessage)).sendAsync().get().getTransactionHash();
            log.info("ETH代币转账,发送方:{"+from+"},接收方:{"+to+"},发送金额:{"+amount+"},hash:{"+hash+"}",new Object[]{from,to,amount,hash});
            return hash ;
        }catch (Exception e){
            e.printStackTrace();
            log.info("虚拟币ETH代币转账失败，错误代码：{}",new Object[]{e.getMessage()});
            return null ;
        }
    }
    /**
     *@描述 eth 区块查询
     *@参数 [blockNum]
     *@参数 scanBlock 区块高度
     *@返回值 void
     *@修改人和其它信息
     */
    public List<EthBlock.TransactionResult> getBlock(long scanBlock){
        try{
            EthBlock.Block block = web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(BigInteger.valueOf(scanBlock)), true).send().getBlock();
            List<EthBlock.TransactionResult> transactions = block.getTransactions();
            return transactions ;
//            for (int i = blockNum;i<=blockNumber.intValue();i++){
//                log.info("ETH查询当前区块：{}",new Object[]{i});
//                EthBlock.Block block = web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNum)), true).send().getBlock();
//                List<EthBlock.TransactionResult> transactions = block.getTransactions();
//                for (EthBlock.TransactionResult transactionResult:transactions) {
//                    if(transactionResult instanceof  EthBlock.TransactionObject){
//                        EthBlock.TransactionObject tx = (EthBlock.TransactionObject)transactionResult ;
//                        String blockHash = tx.getHash() ;
//                        String from = tx.getFrom();
//                        String to = tx.getTo();
//                        String input = tx.getInput();
//                        BigInteger gas = tx.getGas();
//                        System.out.println("from:"+from);
//                        System.out.println("to:"+to);
//                        System.out.println("gas:"+gas);
//                        System.out.println("hash:"+blockHash);
//                        System.out.println("input:"+input);
//                    }
//                }
//            }
        }catch (Exception e){
            e.printStackTrace();
            log.info("虚拟币ETH查询区块记录错误，错误代码：{"+e.getMessage()+"}",new Object[]{e.getMessage()});
        }throw new RuntimeException() ;
    }
    /**
     *@描述 获取hash转账状态
     *@参数 [hash]
     *@返回值 boolean
     *@修改人和其它信息
     */
    public boolean getHashStatus(String hash){
        boolean flag = true ;
        try{
            EthGetTransactionReceipt send = web3j.ethGetTransactionReceipt(hash).send();
            String status = send.getResult().getStatus();
            BigInteger integer = Numeric.decodeQuantity(status);
            BigInteger bigInteger = new BigInteger("1");
            if(bigInteger.compareTo(integer) == 0){
                flag = false ;
                return flag ;
            }
            return flag ;
        }catch (Exception e){
            log.info("获取hash转账状态失败！");
        }
        return flag ;
    }
}
