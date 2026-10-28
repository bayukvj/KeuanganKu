const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read transactions", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("transactions").get());
});

test("Authenticated user (Alice): can create transaction for herself", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const txRef = aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_001");
  await assertSucceeds(
    txRef.set({
      userId: ALICE_UID,
      type: "EXPENSE",
      amount: 50000,
      category: "Belanja",
      title: "Belanja Bulanan",
      note: "Beli beras dan minyak",
      date: Date.now(),
      account: "Tunai",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user (Alice): cannot create transaction with negative amount", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const txRef = aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_002");
  await assertFails(
    txRef.set({
      userId: ALICE_UID,
      type: "EXPENSE",
      amount: -1000,
      category: "Belanja",
      title: "Minus",
      date: Date.now(),
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user (Bob): cannot read or write Alice's transactions", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("transactions").doc("tx_alice").set({
      userId: ALICE_UID,
      type: "INCOME",
      amount: 10000000,
      category: "Gaji",
      title: "Gaji Kantor",
      date: Date.now(),
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_alice").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("transactions").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_bob_hack").set({
      userId: BOB_UID,
      type: "EXPENSE",
      amount: 50000,
      category: "Belanja",
      title: "Hack",
      date: Date.now(),
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user (Alice): can create and update category budget", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const budgetRef = aliceDb.collection("users").doc(ALICE_UID).collection("budgets").doc("budget_belanja");
  
  // Create budget for Belanja
  await assertSucceeds(
    budgetRef.set({
      userId: ALICE_UID,
      category: "Belanja",
      amount: 2000000,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  // Update budget amount
  await assertSucceeds(
    budgetRef.update({
      amount: 2500000,
      updatedAt: new Date(),
    })
  );

  // Bob cannot read Alice's budget
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("budgets").doc("budget_belanja").get()
  );
});
