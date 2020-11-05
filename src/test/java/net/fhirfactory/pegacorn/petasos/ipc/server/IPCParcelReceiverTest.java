package net.fhirfactory.pegacorn.petasos.ipc.server;

import net.fhirfactory.pegacorn.petasos.ipc.model.IPCPacketFramingConstants;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenFormatStage;
import org.jboss.shrinkwrap.resolver.api.maven.MavenStrategyStage;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.time.Instant;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@RunWith(Arquillian.class)
public class IPCParcelReceiverTest {
    private static final Logger LOG = LoggerFactory.getLogger(IPCParcelReceiverTest.class);

    IPCPacketFramingConstants framingConstants = new IPCPacketFramingConstants();
    
    @Deployment(testable=false)
    public static WebArchive createDeployment() {
        WebArchive testWAR;

        PomEquippedResolveStage pomEquippedResolver = Maven.resolver().loadPomFromFile("pom.xml");
        PomEquippedResolveStage pomEquippedResolverWithRuntimeDependencies = pomEquippedResolver.importRuntimeDependencies();
        MavenStrategyStage mavenResolver = pomEquippedResolverWithRuntimeDependencies.resolve();
        MavenFormatStage mavenFormat = mavenResolver.withTransitivity();
        File[] fileSet = mavenFormat.asFile();
        LOG.debug(".createDeployment(): ShrinkWrap Library Set for run-time equivalent, length --> {}", fileSet.length);
        for (int counter = 0; counter < fileSet.length; counter++) {
            File currentFile = fileSet[counter];
            LOG.trace(".createDeployment(): Shrinkwrap Entry --> {}", currentFile.getName());
        }
        testWAR = ShrinkWrap.create(WebArchive.class, "pegacorn-petasos-ipc-test.war")
                .addAsLibraries(fileSet)
                .addPackages(true, "net.fhirfactory.pegacorn.petasos.ipc.server")
                .addPackages(true, "net.fhirfactory.pegacorn.petasos.ipc.model")
                .addPackages(true, "net.fhirfactory.pegacorn.petasos.ipc.codecs")
                .addAsManifestResource("META-INF/beans.xml", "WEB-INF/beans.xml");
        if (LOG.isDebugEnabled()) {
            Map<ArchivePath, Node> content = testWAR.getContent();
            Set<ArchivePath> contentPathSet = content.keySet();
            Iterator<ArchivePath> contentPathSetIterator = contentPathSet.iterator();
            while (contentPathSetIterator.hasNext()) {
                ArchivePath currentPath = contentPathSetIterator.next();
                LOG.trace(".createDeployment(): pegacorn-petasos-ipc-test.war Entry Path --> {}", currentPath.get());
            }
        }
        return (testWAR);
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    @RunAsClient
    public void testSimpleRoute() {
        try {
            Socket socket = new Socket("localhost", 13131);
            OutputStream outputStream = socket.getOutputStream();
            InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
            BufferedReader incomingStream = new BufferedReader(streamReader);
            String testMessage =  "Howdie Doodie" + framingConstants.getIpcPacketFrameEnd();
            outputStream.write(testMessage.getBytes());
            String incomingString = incomingStream.readLine();
            LOG.info(".testSimpleRoute(): Feedback1 --> {}", incomingString);
            String testMessage2 = this.apInvoicesValidString + framingConstants.getIpcPacketFrameEnd();
            outputStream.write(testMessage2.getBytes());
            String incomingString2 = readResponse(streamReader);
            LOG.info(".testSimpleRoute(): Feedback2 --> {}", incomingString2);
            Thread.sleep(2000);
            outputStream.write(testMessage.getBytes());
            String incomingString3 = incomingStream.readLine();
            LOG.info(".testSimpleRoute(): Feedback3 --> {}", incomingString3);
            Thread.sleep(5000);
            outputStream.close();

        } catch (Exception ex){
            LOG.error("Error --> {}", ex);
        }
    }

    private String readResponse(InputStreamReader reader){
        Long readStartTime = Date.from(Instant.now()).getTime();
        Long maxWaitTime = Long.valueOf(15000);
        StringBuilder payload = new StringBuilder();
        int incoming;
        boolean frameFound = false;
        boolean timedOut = false;
        try {
            while(!frameFound && !timedOut) {
                incoming = reader.read();
                if(incoming != -1){
                    payload.append((char) incoming);
                }
                if (payload.toString().contains("<|><ETX><|>")) {
                    frameFound = true;
                }
                Long timeNow = Date.from(Instant.now()).getTime();
                if (timeNow > (readStartTime + maxWaitTime)) {
                    timedOut = true;
                    return ("Empty");
                }
            }
        } catch( Exception ex){
            return("Failed");
        }
        return(payload.toString());
    }

    String apInvoicesValidString =
            "{\n" +
                    "   \"TransactionList\":[\n" +
                    "      {\n" +
                    "         \"SupplierName\":\"2\",\n" +
                    "         \"SupplierNumber\":\"3\",\n" +
                    "         \"SupplierSiteCode\":\"4\",\n" +
                    "         \"InvoiceNumber\":\"5\",\n" +
                    "         \"InvoiceDate\":\"12-JAN-20\",\n" +
                    "         \"InvoiceAmount\":\"-700\",\n" +
                    "         \"InvoiceDesc\":\"8\",\n" +
                    "         \"InvoiceDateReceived\":\"12-JAN-20\",\n" +
                    "         \"TransactionLine\":[\n" +
                    "            {\n" +
                    "               \"LineType\":\"ITEM\",\n" +
                    "               \"Description\":\"6\",\n" +
                    "               \"LineAmount\":\"-200.50\",\n" +
                    "               \"Entity\":\"8\",\n" +
                    "               \"CostCentre\":\"9\",\n" +
                    "               \"Natural\":\"10\",\n" +
                    "               \"InternalTrading\":\"11\",\n" +
                    "               \"Project\":\"12\",\n" +
                    "               \"Agency\":\"13\",\n" +
                    "               \"Spare\":\"9999\",\n" +
                    "               \"TaxCode\":\"B\",\n" +
                    "               \"TaskNumber\":\"123\",\n" +
                    "               \"ProjectNumber\":\"xyx\",\n" +
                    "               \"ExpenditureType\" : \"A\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "               \"LineType\":\"ITEM\",\n" +
                    "               \"Description\":\"6\",\n" +
                    "               \"LineAmount\":\"-499.50\",\n" +
                    "               \"Entity\":\"8\",\n" +
                    "               \"CostCentre\":\"9\",\n" +
                    "               \"Natural\":\"10\",\n" +
                    "               \"InternalTrading\":\"11\",\n" +
                    "               \"Project\":\"12\",\n" +
                    "               \"Agency\":\"13\",\n" +
                    "               \"Spare\":\"9999\",\n" +
                    "               \"TaxCode\":\"B\",\n" +
                    "               \"TaskNumber\":\"123\",\n" +
                    "               \"ProjectNumber\":\"xyx\",\n" +
                    "               \"ExpenditureType\" : \"A\"\n" +
                    "            }\n" +
                    "         ]\n" +
                    "      },\n" +
                    "      {\n" +
                    "         \"SupplierName\":\"4\",\n" +
                    "         \"SupplierNumber\":\"30\",\n" +
                    "         \"SupplierSiteCode\":\"4\",\n" +
                    "         \"InvoiceNumber\":\"3\",\n" +
                    "         \"InvoiceDate\":\"12-JAN-20\",\n" +
                    "         \"InvoiceAmount\":\"560\",\n" +
                    "         \"InvoiceDesc\":\"8\",\n" +
                    "         \"InvoiceDateReceived\":\"12-JAN-20\",\n" +
                    "         \"TransactionLine\":[\n" +
                    "            {\n" +
                    "               \"LineType\":\"ITEM\",\n" +
                    "               \"Description\":\"6\",\n" +
                    "               \"LineAmount\":\"50\",\n" +
                    "               \"Entity\":\"8\",\n" +
                    "               \"CostCentre\":\"9\",\n" +
                    "               \"Natural\":\"10\",\n" +
                    "               \"InternalTrading\":\"11\",\n" +
                    "               \"Project\":\"12\",\n" +
                    "               \"Agency\":\"13\",\n" +
                    "               \"Spare\":\"9999\",\n" +
                    "               \"TaxCode\":\"B\",\n" +
                    "               \"TaskNumber\":\"123\",\n" +
                    "               \"ProjectNumber\":\"xyx\",\n" +
                    "               \"ExpenditureType\" : \"A\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "               \"LineType\":\"ITEM\",\n" +
                    "               \"Description\":\"6\",\n" +
                    "               \"LineAmount\":\"510.00\",\n" +
                    "               \"Entity\":\"8\",\n" +
                    "               \"CostCentre\":\"9\",\n" +
                    "               \"Natural\":\"10\",\n" +
                    "               \"InternalTrading\":\"11\",\n" +
                    "               \"Project\":\"12\",\n" +
                    "               \"Agency\":\"13\",\n" +
                    "               \"Spare\":\"9999\",\n" +
                    "               \"TaxCode\":\"B\",\n" +
                    "               \"TaskNumber\":\"123\",\n" +
                    "               \"ProjectNumber\":\"xyx\",\n" +
                    "               \"ExpenditureType\" : \"A\"\n" +
                    "            }\n" +
                    "         ]\n" +
                    "      }\n" +
                    "   ]\n" +
                    "}";

}
