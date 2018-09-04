package stest.tron.wallet.committee;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.gsc.api.WalletGrpc;
import org.gsc.api.WalletSolidityGrpc;
import org.gsc.core.Wallet;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.utils.PublicMethed;


@Slf4j
public class WalletTestCommittee002 {
  //from account
  private final String testKey003 =
      "6815B367FDDE637E53E9ADC8E69424E07724333C9A2B973CFA469975E20753FC";
  //Witness 47.93.9.236
  private final String witnessKey001 =
      "369F095838EB6EED45D4F6312AF962D5B9DE52927DA9F04174EE49F9AF54BC77";
  //Witness 47.93.33.201
  private final String witnessKey002 =
      "9FD8E129DE181EA44C6129F727A6871440169568ADE002943EAD0E7A16D8EDAC";
  //Witness 123.56.10.6
  private final String witnessKey003 =
      "291C233A5A7660FB148BAE07FCBCF885224F2DF453239BD983F859E8E5AA4602";
  //Wtiness 39.107.80.135
  private final String witnessKey004 =
      "99676348CBF9501D07819BD4618ED885210CB5A03FEAF6BFF28F0AF8E1DE7DBE";
  //Witness 47.93.184.2
  private final String witnessKey005 =
      "FA090CFB9F3A6B00BE95FE185E82BBCFC4DA959CA6A795D275635ECF5D58466D";


  private final byte[] toAddress = PublicMethed.getFinalAddress(testKey003);
  private final byte[] witness001Address = PublicMethed.getFinalAddress(witnessKey001);
  private final byte[] witness002Address = PublicMethed.getFinalAddress(witnessKey002);
  private final byte[] witness003Address = PublicMethed.getFinalAddress(witnessKey003);
  private final byte[] witness004Address = PublicMethed.getFinalAddress(witnessKey004);
  private final byte[] witness005Address = PublicMethed.getFinalAddress(witnessKey005);


  private ManagedChannel channelFull = null;
  private ManagedChannel channelSolidity = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity = null;

  private static final long now = System.currentTimeMillis();

  private String fullnode = Configuration.getByPath("testng.conf").getStringList("fullnode.ip.list")
      .get(0);
  private String soliditynode = Configuration.getByPath("testng.conf")
      .getStringList("solidityNode.ip.list").get(0);

  @BeforeSuite
  public void beforeSuite() {
    Wallet wallet = new Wallet();
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
  }

  @BeforeClass
  public void beforeClass() {
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);

    channelSolidity = ManagedChannelBuilder.forTarget(soliditynode)
        .usePlaintext(true)
        .build();
    blockingStubSolidity = WalletSolidityGrpc.newBlockingStub(channelSolidity);
    Assert.assertTrue(PublicMethed.sendcoin(witness001Address,10000000L,
        toAddress,testKey003,blockingStubFull));
  }


  @Test(enabled = true)
  public void testCreateProposalMaintenanceTimeInterval() {
    //0:MAINTENANCE_TIME_INTERVAL,[3*27s,24h]
    //Minimum interval
    HashMap<Long, Long> proposalMap = new HashMap<Long, Long>();
    proposalMap.put(0L, 81000L);
    Assert.assertTrue(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Maximum interval
    proposalMap.put(0L, 86400000L);
    Assert.assertTrue(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Minimum -1 interval, create failed.
    proposalMap.put(0L, 80000L);
    Assert.assertFalse(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Maximum + 1 interval
    proposalMap.put(0L, 86401000L);
    Assert.assertFalse(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Non witness account
    proposalMap.put(0L, 86400000L);
    Assert.assertFalse(PublicMethed.createProposal(toAddress,testKey003,proposalMap,
        blockingStubFull));
  }

  @Test(enabled = true)
  public void testCreateProposalAccountUpgradeCost() {
    //1:ACCOUNT_UPGRADE_COST,[0,100 000 000 000 000 000]//drop
    //Minimum AccountUpgradeCost
    HashMap<Long, Long> proposalMap = new HashMap<Long, Long>();
    proposalMap.put(1L, 0L);
    Assert.assertTrue(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Maximum AccountUpgradeCost
    proposalMap.put(1L, 100000000000000000L);
    Assert.assertTrue(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Minimum - 1 AccountUpgradeCost
    proposalMap.put(1L, -1L);
    Assert.assertFalse(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Maximum + 1 AccountUpgradeCost
    proposalMap.put(1L, 100000000000000001L);
    Assert.assertFalse(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Non witness account
    proposalMap.put(1L, 86400000L);
    Assert.assertFalse(PublicMethed.createProposal(toAddress,testKey003,
        proposalMap,blockingStubFull));
  }

  @Test(enabled = true)
  public void testCreateProposalCreateAccountFee() {
    //2:CREATE_ACCOUNT_FEE,[0,100 000 000 000 000 000]//drop
    //Minimum CreateAccountFee
    HashMap<Long, Long> proposalMap = new HashMap<Long, Long>();
    proposalMap.put(2L, 0L);
    Assert.assertTrue(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Maximum CreateAccountFee
    proposalMap.put(2L, 100000000000000000L);
    Assert.assertTrue(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Minimum - 1 CreateAccountFee
    proposalMap.put(2L, -1L);
    Assert.assertFalse(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Maximum + 1 CreateAccountFee
    proposalMap.put(2L, 100000000000000001L);
    Assert.assertFalse(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Non witness account
    proposalMap.put(2L, 86400000L);
    Assert.assertFalse(PublicMethed.createProposal(toAddress,testKey003,
        proposalMap,blockingStubFull));

  }

  @Test(enabled = true)
  public void testTransactionFee() {
    //3:TRANSACTION_FEE,[0,100 000 000 000 000 000]//drop
    //Minimum TransactionFee
    HashMap<Long, Long> proposalMap = new HashMap<Long, Long>();
    proposalMap.put(3L, 0L);
    Assert.assertTrue(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Maximum TransactionFee
    proposalMap.put(3L, 100000000000000000L);
    Assert.assertTrue(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Minimum - 1 TransactionFee
    proposalMap.put(3L, -1L);
    Assert.assertFalse(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Maximum + 1 TransactionFee
    proposalMap.put(3L, 100000000000000001L);
    Assert.assertFalse(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Non witness account
    proposalMap.put(3L, 86400000L);
    Assert.assertFalse(PublicMethed.createProposal(toAddress,testKey003,
        proposalMap,blockingStubFull));

  }

  @Test(enabled = true)
  public void testAssetIssueFee() {
    //4:ASSET_ISSUE_FEE,[0,100 000 000 000 000 000]//drop
    //Minimum AssetIssueFee
    HashMap<Long, Long> proposalMap = new HashMap<Long, Long>();
    proposalMap.put(4L, 0L);
    Assert.assertTrue(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Duplicat proposals
    proposalMap.put(4L, 0L);
    Assert.assertTrue(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Maximum AssetIssueFee
    proposalMap.put(4L, 100000000000000000L);
    Assert.assertTrue(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Minimum - 1 AssetIssueFee
    proposalMap.put(4L, -1L);
    Assert.assertFalse(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Maximum + 1 AssetIssueFee
    proposalMap.put(4L, 100000000000000001L);
    Assert.assertFalse(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Non witness account
    proposalMap.put(4L, 86400000L);
    Assert.assertFalse(PublicMethed.createProposal(toAddress,testKey003,
        proposalMap,blockingStubFull));

  }

  @Test(enabled = true)
  public void testWitnessPayPerBlock() {
    //5:WITNESS_PAY_PER_BLOCK,[0,100 000 000 000 000 000]//drop
    //Minimum WitnessPayPerBlock
    HashMap<Long, Long> proposalMap = new HashMap<Long, Long>();
    proposalMap.put(5L, 0L);
    Assert.assertTrue(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Maximum WitnessPayPerBlock
    proposalMap.put(5L, 100000000000000000L);
    Assert.assertTrue(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Minimum - 1 WitnessPayPerBlock
    proposalMap.put(5L, -1L);
    Assert.assertFalse(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Maximum + 1 WitnessPayPerBlock
    proposalMap.put(5L, 100000000000000001L);
    Assert.assertFalse(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Non witness account
    proposalMap.put(5L, 86400000L);
    Assert.assertFalse(PublicMethed.createProposal(toAddress,testKey003,
        proposalMap,blockingStubFull));

  }

  @Test(enabled = true)
  public void testWitnessStandbyAllowance() {
    //6:WITNESS_STANDBY_ALLOWANCE,[0,100 000 000 000 000 000]//drop
    //Minimum WitnessStandbyAllowance
    HashMap<Long, Long> proposalMap = new HashMap<Long, Long>();
    proposalMap.put(6L, 0L);
    Assert.assertTrue(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Maximum WitnessStandbyAllowance
    proposalMap.put(6L, 100000000000000000L);
    Assert.assertTrue(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Minimum - 1 WitnessStandbyAllowance
    proposalMap.put(6L, -1L);
    Assert.assertFalse(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Maximum + 1 WitnessStandbyAllowance
    proposalMap.put(6L, 100000000000000001L);
    Assert.assertFalse(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Non witness account
    proposalMap.put(6L, 86400000L);
    Assert.assertFalse(PublicMethed.createProposal(toAddress,testKey003,
        proposalMap,blockingStubFull));

  }

  @Test(enabled = true)
  public void testCreateNewAccountFeeInSystemControl() {
    //7:CREATE_NEW_ACCOUNT_FEE_IN_SYSTEM_CONTRACT,0 or 1
    HashMap<Long, Long> proposalMap = new HashMap<Long, Long>();
    proposalMap.put(7L, 1L);
    Assert.assertTrue(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Maximum WitnessStandbyAllowance
    proposalMap.put(7L, 100000000000000000L);
    Assert.assertTrue(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Minimum - 1 WitnessStandbyAllowance
    proposalMap.put(6L, -1L);
    Assert.assertFalse(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Maximum + 1 WitnessStandbyAllowance
    proposalMap.put(6L, 100000000000000001L);
    Assert.assertFalse(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //Non witness account
    proposalMap.put(6L, 86400000L);
    Assert.assertFalse(PublicMethed.createProposal(toAddress,testKey003,
        proposalMap,blockingStubFull));

  }



  @Test(enabled = true)
  public void testInvalidProposals() {
    // The index isn't from 0-9
    HashMap<Long, Long> proposalMap = new HashMap<Long, Long>();
    proposalMap.put(10L, 60L);
    Assert.assertFalse(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));

    //The index is -1
    proposalMap.put(-1L, 6L);
    Assert.assertFalse(PublicMethed.createProposal(witness001Address,witnessKey001,
        proposalMap,blockingStubFull));


  }

  @AfterClass
  public void shutdown() throws InterruptedException {
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    if (channelSolidity != null) {
      channelSolidity.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}

