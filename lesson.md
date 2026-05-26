# Selenium & Healenium - Demo Guide

**Duration:** ~20 minutes

---

## Table of Contents

- [Selenium \& Healenium - Demo Guide](#selenium--healenium---demo-guide)
  - [Table of Contents](#table-of-contents)
  - [Prerequisites](#prerequisites)
  - [Setup Requirements](#setup-requirements)
  - [Demo Objectives](#demo-objectives)
  - [Manual Testing](#manual-testing)
    - [The happy path](#the-happy-path)
    - [The sad path](#the-sad-path)
  - [What is Selenium?](#what-is-selenium)
  - [Selenium Automation](#selenium-automation)
  - [Why This Matters: Regression Testing](#why-this-matters-regression-testing)
    - [The scale problem](#the-scale-problem)
  - [Running Both Suites](#running-both-suites)
  - [The Learning Run](#the-learning-run)
  - [The Problem: Brittle Tests](#the-problem-brittle-tests)
  - [Healenium](#healenium)
    - [The one line that makes this possible](#the-one-line-that-makes-this-possible)
    - [What if you delete the locator entirely?](#what-if-you-delete-the-locator-entirely)
    - [Restore before continuing](#restore-before-continuing)
    - [The side-by-side result](#the-side-by-side-result)
  - [The Dashboard](#the-dashboard)
    - [What the score means](#what-the-score-means)
    - [Would you get this report in a CI/CD pipeline?](#would-you-get-this-report-in-a-cicd-pipeline)
    - [Would you update the locators after healing?](#would-you-update-the-locators-after-healing)
    - [Restore the HTML](#restore-the-html)
  - [Healenium in the Real World](#healenium-in-the-real-world)
  - [Benefits and Limitations](#benefits-and-limitations)
    - [Benefits](#benefits)
    - [Limitations](#limitations)
    - [Summary](#summary)
  - [What Happens When You Restore the Original Locator?](#what-happens-when-you-restore-the-original-locator)
  - [Further Reading](#further-reading)

---

## Prerequisites

- Java 21+ and IntelliJ installed
- Maven 3.8+ installed
- Google Chrome installed
- Docker Desktop installed (required for the Healenium backend only)

&nbsp;
[Back to Contents](#table-of-contents)
&nbsp;

## Setup Requirements

> #### TRAINER PROMPT
>
> The lesson version of this demo can be found [here](./lesson)


[**Everything is self-contained in this Maven project.**](./selenium-tests/) The login page lives in `src/test/resources/webapp/` and the tests open it directly as a local file - no web server required.

Docker is only required for the **Healenium backend**.

**Before the demo, verify the following:**

1. Confirm Java is available:
   ```bash
   java -version
   ```
2. Confirm Maven is available:
   ```bash
   mvn -version
   ```
3. Confirm Docker is running:
   ```bash
   docker ps
   ```
4. Open `src/test/resources/webapp/index.html` directly in Chrome to confirm the page loads

> #### TRAINER PROMPT
>
> Start the Healenium Docker containers **before the session begins** - they take 20-30 seconds to initialise.
>
> ```bash
> cd selenium-tests
> docker-compose up -d
> ```
>
> Also run the Selenium tests to confirm Chrome opens cleanly:
> ```bash
> mvn clean test -Dtest=LoginTestSelenium
> ```
>
> Do **not** run the Healenium tests beforehand. The learning run is part of the demo and the audience should see it happen.

&nbsp;
[Back to Contents](#table-of-contents)
&nbsp;

## Demo Objectives

**By the end of this demo, the audience will have seen:**

- The happy and sad paths of the login page tested manually
- How Selenium automates those same steps
- Why automated regression testing matters at scale
- Healenium recording a baseline of locators during a learning run
- A single HTML attribute change breaking an entire test suite
- Healenium recovering those tests without any code changes
- The Healenium dashboard showing what was healed and with what confidence

> #### TRAINER PROMPT
>
> Frame this upfront: Healenium is not something we are teaching today and it is not yet widely used - it captures roughly 1-3% of the global test automation market. But it is a genuinely interesting tool that illustrates an important idea: what if tests could recover from minor UI changes automatically? That question is worth exploring, and the demo makes it concrete.

&nbsp;
[Back to Contents](#table-of-contents)
&nbsp;

## Manual Testing

Before writing a line of test code, walk through the page as a manual tester would.

Open `src/test/resources/webapp/index.html` in Chrome.

### The happy path

A **happy path** tests that the system works correctly when used as intended.

1. Enter username: `sparta`
2. Enter password: `correct`
3. Click **Login**
4. Expected result: a popup appears saying "You have signed in successfully"

### The sad path

A **sad path** tests that the system handles incorrect input gracefully.

**Sad path 1 - wrong password:**

1. Refresh the page
2. Enter username: `sparta`, password: `wrongpassword`
3. Click **Login**
4. Expected result: "Incorrect details" popup

**Sad path 2 - empty fields:**

1. Refresh the page
2. Leave both fields blank, click **Login**
3. Expected result: "Incorrect details" popup

> #### TRAINER PROMPT
>
> After running through all three manually, ask: "How long did that take?" (About 2-3 minutes.) "If there were 200 test scenarios and we deployed three times a day, what would the testing cost be?" Let the audience calculate it themselves. The motivation for automation follows naturally.

&nbsp;
[Back to Contents](#table-of-contents)
&nbsp;

## What is Selenium?

**Selenium WebDriver** is a tool that lets you control a web browser programmatically. Rather than a person clicking and typing, your code does it. Selenium can open a URL, find elements on the page - buttons, input fields, text - and interact with them exactly as a user would.

Selenium finds elements using **locators** - ways to uniquely identify each element by its HTML attributes. Open `src/test/resources/webapp/index.html`, right-click the Login button and select Inspect:

```html
<button type="submit" id="login-button">Login</button>
```

In a Selenium test, that element is found like this:

```java
webDriver.findElement(By.id("login-button"))
```

The most common locator types are:

| Locator | Example |
|---|---|
| By ID | `By.id("login-button")` |
| By Name | `By.name("user-name")` |
| By CSS Selector | `By.cssSelector("[data-test='error']")` |

> #### TRAINER PROMPT
>
> Press `Ctrl+F` inside the DevTools Elements panel and type `[data-test='error']` - watch it highlight the matching element live. Ask: "Why might `data-test` attributes be preferable to IDs?" Answer: `data-test` attributes exist specifically for testing. A developer reskinning the UI is unlikely to remove an attribute that exists solely to support tests.

&nbsp;
[Back to Contents](#table-of-contents)
&nbsp;

## Selenium Automation

Open `LoginTestSelenium.java`. Each test automates exactly what was done manually.

```java
@Test
@Title("Given valid credentials, when I click Login, then the success popup appears")
public void successfulLoginTest() {
    WebElement usernameField = webDriver.findElement(By.name("user-name"));
    WebElement passwordField = webDriver.findElement(By.name("password"));
    WebElement loginButton   = webDriver.findElement(By.id("login-button"));

    usernameField.sendKeys("sparta");
    passwordField.sendKeys("correct");
    loginButton.click();

    WebElement successPopup = new WebDriverWait(webDriver, Duration.ofSeconds(5))
            .until(ExpectedConditions.visibilityOfElementLocated(By.id("success-message")));
    MatcherAssert.assertThat(successPopup.getText(), containsString("You have signed in successfully"));
}
```

The structure is **Arrange / Act / Assert** - a pattern that mirrors the manual steps precisely. `findElement` locates the element, `sendKeys` types into it, `click` clicks it, and the assertion checks what appears. There is no magic.

The `@Managed` annotation tells Serenity to manage the driver lifecycle - Chrome opens before each test and closes after. No `@BeforeEach` or `@AfterEach` needed.

Run the tests:

```bash
mvn clean test -Dtest=LoginTestSelenium
```

```
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

The Serenity HTML report is at `target/site/serenity/index.html`.

&nbsp;
[Back to Contents](#table-of-contents)
&nbsp;

## Why This Matters: Regression Testing

**Regression testing** checks that existing features still work after new code is added. Every time a developer makes a change, there is a risk that something that previously worked no longer does. Automated regression tests catch this before it reaches production.

### The scale problem

A modest application might need 200 test scenarios. A manual tester takes roughly 2 minutes per scenario:

```
200 scenarios x 2 minutes  = 6 hours 40 minutes per build
3 deployments per day       = 20 hours of manual testing
```

Nobody has 20 hours of testing capacity per day. Automated tests run the same 200 scenarios in minutes, unattended, on every commit. If something breaks, the pipeline turns red and the developer is notified before the code reaches users.

&nbsp;
[Back to Contents](#table-of-contents)
&nbsp;

## Running Both Suites

You also have `LogInTestHealenium` - an identical set of tests using a different driver underneath.

> #### TRAINER PROMPT
>
> Before running, show what Docker is doing. Run:
> ```bash
> docker ps
> ```
> You will see two running containers:
> - `healenium` - a small web server on port 7878 that runs the healing algorithm
> - `healenium-db` - a PostgreSQL database that stores the locator history
>
> Explain simply: "One is a database that stores a record of every element our tests find. The other is a small server that does the comparison when a locator breaks. Without these two things running, Healenium just behaves like normal Selenium."
>
> Then mention briefly that Healenium requires a small amount of additional setup - a properties file and a custom driver class. That setup is already done and is not the focus of the demo. The test logic is identical to the Selenium version.

Run both together:

```bash
mvn clean test -Dtest="LoginTestSelenium,LogInTestHealenium"
```

```
LoginTestSelenium:   Tests run: 4, Failures: 0, Errors: 0
LogInTestHealenium:  Tests run: 4, Failures: 0, Errors: 0
```

Both pass. Open `target/site/serenity/index.html` to show both test suites listed side by side.

> #### TRAINER PROMPT
>
> Ask the room: "If both pass and do the same thing, why do we need Healenium at all?" Take a few answers - you will usually get "for when things break" which is exactly right. That is what comes next.

&nbsp;
[Back to Contents](#table-of-contents)
&nbsp;

## The Learning Run

Before Healenium can heal anything, it needs to see the tests pass against a working page. This is called the **learning run** - Healenium records every element it successfully finds and saves the location to the database.

Run the Healenium tests now against the unmodified page:

```bash
mvn clean test -Dtest=LogInTestHealenium
```

Watch the Docker logs in a second terminal while this runs:

```bash
docker-compose logs -f healenium
```

You will see lines like:

```
[Save Elements] Request: By.name(user-name)
[Save Elements] Request: By.name(password)
[Save Elements] Request: By.id(login-button)
```

Every element the tests interact with is being saved to the database. This is the baseline. Without this step, Healenium has nothing to compare against when a locator breaks.

> #### TRAINER PROMPT
>
> This is the moment to explain the dependency: "Healenium cannot heal a locator it has never seen succeed. If you deploy this to a brand new environment, you need to run the tests against a working page first. After that, the database persists across runs and healing works automatically."

&nbsp;
[Back to Contents](#table-of-contents)
&nbsp;

## The Problem: Brittle Tests

A **brittle** test breaks for reasons unrelated to the behaviour being tested. It does not break because the feature is broken - it breaks because a minor structural detail of the page changed.

Open `src/test/resources/webapp/index.html` and make this single change to the login button:

```html
<!-- Before -->
<button type="submit" id="login-button">Login</button>

<!-- After - simulating a developer rename -->
<button type="submit" id="loginBtn">Login</button>
```

Save the file. Open it in Chrome and log in manually - the button still works. The feature is not broken.

Now re-run the Selenium tests:

```bash
mvn clean test -Dtest=LoginTestSelenium
```

```
[ERROR] Tests run: 4, Failures: 0, Errors: 4, Skipped: 0

org.openqa.selenium.NoSuchElementException:
no such element: Unable to locate element: {"method":"id","selector":"login-button"}
```

Every test that uses the login button fails. Not because the login feature is broken - the button works fine in the browser. They fail because `id="login-button"` no longer exists and Selenium cannot find it.

> #### TRAINER PROMPT
>
> Open the browser with the page still loaded and manually log in. The popup appears. The feature works. Then show the failing test output side by side. This contrast - working feature, failing tests - is the sharpest way to explain what "brittle" means. Ask: "If this happened every time a developer touched the UI, what would eventually happen to your test suite?"

&nbsp;
[Back to Contents](#table-of-contents)
&nbsp;

## Healenium

Now run the Healenium test suite against the same broken page:

```bash
mvn clean test -Dtest=LogInTestHealenium
```

```
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

All four tests pass. No test code was changed. No locators were updated.

### The one line that makes this possible

Open `HealeniumDriverSource.java`:

```java
WebDriver baseDriver = new ChromeDriver(options);
return SelfHealingDriver.create(baseDriver);  // this line
```

`SelfHealingDriver` wraps the standard `ChromeDriver`. On every successful `findElement`, it saves the element's location in the DOM to the database. When a `findElement` fails, instead of throwing immediately, it compares the stored location against every element currently on the page, scores each one by similarity, and uses the best match to complete the action.

### What if you delete the locator entirely?

Renaming the `id` is one scenario. What if a developer removes it entirely?

In `src/test/resources/webapp/index.html`, change the button to have no `id` at all:

```html
<!-- Before -->
<button type="submit" id="login-button">Login</button>

<!-- After - id attribute removed entirely -->
<button type="submit">Login</button>
```

Run the Selenium tests:

```bash
mvn clean test -Dtest=LoginTestSelenium
```

Same failure - `By.id("login-button")` finds nothing.

Now run Healenium:

```bash
mvn clean test -Dtest=LogInTestHealenium
```

Still passes. The button is in exactly the same place in the DOM - same tag, same position inside the form. The `id` is gone but the structural path is near-identical, so the similarity score remains high and Healenium heals to a position-based selector instead.

This demonstrates that Healenium is comparing the **structure and position** of elements, not just their attributes. As long as the element is still approximately where it was in the DOM tree, healing has a strong chance of succeeding.

### Restore before continuing

Put the original button back before showing the dashboard:

```html
<button type="submit" id="loginBtn">Login</button>
```

Keep the renamed version (not the deleted version) so the dashboard shows a clear before and after.

### The side-by-side result

Run both together while the locator is still renamed:

```bash
mvn clean test -Dtest="LoginTestSelenium,LogInTestHealenium"
```

```
LoginTestSelenium:   Tests run: 4, Errors: 4
LogInTestHealenium:  Tests run: 4, Errors: 0
```

Same test logic. Same page. Same browser. The only difference is the driver.

&nbsp;
[Back to Contents](#table-of-contents)
&nbsp;

## The Dashboard

Open `http://localhost:7878/healenium/report` in your browser.

The report shows the healing events from the test run:

- **Failed Locator** - the original locator that broke: `By.id(login-button)`
- **Healed Locator** - what Healenium used instead: `By.cssSelector(button#loginBtn)`
- **Score** - the confidence level of the match

### What the score means

The score is a number between 0 and 1 representing how similar the healed element is to the original. It comes from the **LSC (Longest Common Subsequence) algorithm**, which compares the stored path to the element through the DOM tree against all candidate elements in the current page.

A score of `0.99` means the algorithm is 99% confident it found the right element. In this case, the button is in exactly the same place in the DOM - only the `id` changed slightly. Almost everything matches, so the score is very high.

| Score | What it means |
|---|---|
| 0.9 to 1.0 | Near-certain match - only a minor attribute changed |
| 0.6 to 0.9 | Reasonable match - some structural changes but likely the same element |
| Below 0.6 | Below the confidence threshold - Healenium does not attempt to heal |

The `score-cap` in `healenium.properties` sets that threshold. Any candidate scoring below `0.6` is rejected and Healenium throws the original exception instead of guessing.

### Would you get this report in a CI/CD pipeline?

Yes - if the Docker containers are running as part of your pipeline, Healenium records healing events the same way regardless of where the tests run. The report is generated per session and accessible at `localhost:7878/healenium/report` (or whatever host the backend is deployed to). In a CI environment, teams typically point the backend at a shared server so the healing history persists across builds and the report is accessible to the whole team.

### Would you update the locators after healing?

Yes. Healenium is a safety net, not a permanent fix. After Healenium heals a locator, you should go into your test code and update it to use the correct locator permanently. The dashboard is the signal that tells you what needs updating - it shows you exactly which locator broke, what it was replaced with, and how confident the algorithm was. Leaving tests permanently relying on healed locators means the tests are running against a different selector than you think.

### Restore the HTML

```html
<button type="submit" id="login-button">Login</button>
```

Confirm both suites pass cleanly:

```bash
mvn clean test -Dtest="LoginTestSelenium,LogInTestHealenium"
```

Then stop the containers:

```bash
docker-compose down
```

&nbsp;
[Back to Contents](#table-of-contents)
&nbsp;

## Healenium in the Real World

- **Not yet widely adopted** - roughly 1-3% of the global test automation market. Most modern teams lean towards Playwright, Cypress, or commercial platforms like mabl and Testim.
- **Strongest in Java and Selenium teams** - particularly large enterprises with established regression suites they cannot easily migrate. In that niche, adoption is estimated at 12-20%.
- **Created by EPAM Systems** - a major global software engineering consultancy who use it as a default tool for Fortune 500 clients.
- **Used in enterprise** - healthcare platforms like Athenahealth and Tempus AI, and consulting firms including IWConnect and Happiest Minds.
- **Available on AWS Marketplace** as an official Amazon Machine Image. BrowserStack maintains official integration examples.
- **Free open source, with paid tiers** - Healenium Pro ranges from $1,000 to $30,000 per year for commercial support and AI features. For a team already on Java and Selenium, the open source version is a practical low-cost starting point.

> #### TRAINER PROMPT
>
> Ask: "If you were managing a team with 500 automated UI tests and the UI changes every sprint, how would you approach the maintenance problem?" The answers will range from better locator strategy to POM to tools like Healenium to accepting some test churn as normal. There is no single right answer - the point is that the problem is real and teams handle it in different ways.

&nbsp;
[Back to Contents](#table-of-contents)
&nbsp;

## Benefits and Limitations

> #### TRAINER PROMPT
>
> Present this section as a balanced discussion. The limitations are just as important as the benefits. Healenium is a useful tool in the right context, not a universal solution.

### Benefits

**Reduces test maintenance for minor UI changes.** When a developer renames an ID or removes an attribute, Healenium absorbs the impact without any action from the test team.

**No changes to existing test code.** The only code change is wrapping `ChromeDriver` with `SelfHealingDriver`. Every existing `findElement` call works without modification.

**Healing reports.** The dashboard shows the failed locator, the healed locator, and the confidence score - making it straightforward to review what changed and update tests accordingly.

**Self-improving over time.** Once a locator is healed and confirmed, future runs use the corrected locator directly without a healing step.

### Limitations

**Requires Docker infrastructure.** The PostgreSQL backend must be running for Healenium to function. This is an operational dependency that plain Selenium does not have - in a CI/CD pipeline the containers must be started before tests run.

**Needs a learning run first.** Healenium cannot heal a locator it has never seen succeed. A run against a working page is required before any healing baseline exists.

**Does not work for major UI changes.** The algorithm works well when an ID is renamed or removed. If a component is completely removed and rebuilt from scratch, the DOM path is too different to score above the threshold and the test will still fail - just as it would with plain Selenium.

**Can mask genuine failures.** If an element disappears because a feature is genuinely broken, Healenium might find another element with a similar score and interact with the wrong thing. The test passes, but it is testing the wrong behaviour. Healed locators should always be reviewed.

**Does not replace good locator strategy.** `data-test` attributes and semantic IDs are still the foundation. Healenium is a safety net, not a substitute for writing maintainable locators in the first place.

**Not yet widely adopted.** Outside of Java and Selenium teams, Healenium has limited visibility. Teams using Playwright, Cypress, or Python-based stacks will find limited support and tooling.

### Summary

| | Plain Selenium | Healenium |
|---|---|---|
| Maintenance on UI change | Manual update required | Auto-heals minor changes |
| Infrastructure needed | None | Docker + PostgreSQL |
| Failure visibility | Always fails loudly | May heal silently - review healed locators |
| Setup cost | Minimal | Low (one extra line in driver setup) |
| Works for major UI redesigns | Fails | Also fails |
| Best for | Stable UIs, smaller suites | Fast-moving UIs, larger Java-Selenium suites |

&nbsp;
[Back to Contents](#table-of-contents)
&nbsp;

## What Happens When You Restore the Original Locator?

When you put `id="login-button"` back, Healenium does not need to heal anything. The original locator works again, `findElement` succeeds normally, and Healenium simply updates the baseline in the database with the current DOM state. No healing event is triggered or recorded. It behaves exactly like plain Selenium would.

The database is not cleared - it still holds the history of the previous healing event. But on this run, everything succeeds without the healing algorithm being invoked at all.

&nbsp;
[Back to Contents](#table-of-contents)
&nbsp;

## Further Reading

**Healenium**

1. [How Healenium Works](https://healenium.io/docs/how_healenium_works) - the five-stage healing process explained with visuals
2. [Healenium GitHub](https://github.com/healenium/healenium-web) - source code, issues, and latest version
3. [Healenium Backend Setup](https://github.com/healenium/healenium-backend) - the Docker backend repository
4. [Healenium Official Documentation](https://healenium.io/docs/download_and_install/hlm_web) - full installation and configuration guide
5. [Healenium Pro - AI Integration](https://healenium.io/docs/ai) - the commercial tier with GitHub integration and Playwright proxy

**Selenium**

6. [Selenium Official Documentation](https://www.selenium.dev/documentation/) - WebDriver API and guides
7. [Serenity BDD](https://serenity-bdd.info/) - the reporting and lifecycle management layer used in this project
8. [WebDriverManager](https://bonigarcia.dev/webdrivermanager/) - auto-downloads the correct chromedriver version

**Locator Strategy**

9. [CSS Selectors Reference (MDN)](https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_selectors) - complete CSS selector syntax
10. [Testing Library Guiding Principles](https://testing-library.com/docs/guiding-principles/) - why semantic locators outlast structural ones

**The Wider Landscape**

11. [Playwright Documentation](https://playwright.dev/docs/intro) - modern alternative to Selenium with more resilient built-in locators
12. [mabl](https://www.mabl.com/) - commercial AI-powered test runner with self-healing built in
13. [Applitools](https://applitools.com/) - visual AI testing as a complement to DOM-based tests

&nbsp;
[Back to Contents](#table-of-contents)
&nbsp;