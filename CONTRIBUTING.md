# Contributing

So, you want to contribute to this project! That's awesome. However, before doing so, please read the following simple steps how to contribute.


## Always open an issue

Before doing anything always [open an issue](https://github.com/otto-de/edison-microservice/issues), 
describing the contribution you would like to make, the bug you found or any other ideas you have. 
This will help us to get you started on the right foot.

It is recommended to wait for feedback before continuing to next steps. However, if 
the issue is clear (e.g. a typo) and the fix is simple, you can continue and fix it.


## Contact the edison core team

Feel free to contact any of us if you have any questions or need help!

* [Guido Steinacker](https://github.com/gsteinacker) aka "The Author"
* [Matthias Bargmann](https://github.com/mazzeb)
* [Peter Fouquet](https://github.com/pfouquet1)
* [Benedikt Stemmildt](https://github.com/BeneStem)

Our colleagues at OTTO can use the chat group "edison-microservice" to contact us.


## How to change code

Fork the project in your account and create a branch with your fix or new feature: some-great-feature or some-issue-fix.
Commit your changes in that branch, writing the code following the code style.

* Edit or create tests to ensure a high coverage.
* Always edit or create documentation.

### Commit message format

To ensure a unified view to the changes please use [this commit message format](https://chris.beams.io/posts/git-commit/#seven-rules).
Example (with a way too long subject line):

    [edison-jobs][breaking change]: Write subject in imperative mood wihtout a period at the end
    
    - Separate subject from body with a blank line
    - Please use bulletpoints in the body
    - Typically a hyphen is used for the bullet, preceded
      by a single space, with blank lines in between, but conventions
      vary here
    
    Resolves: #123
    See also: #456, #789


### Maintain the CHANGELOG.md

Add all noticeable changes to the [CHANGELOG.md](CHANGELOG.md).
To ensure a unified view to the changes please format the [CHANGELOG.md](CHANGELOG.md) as done [in this example](https://github.com/skywinder/github-changelog-generator/blob/master/CHANGELOG.md). 


### Create a pull request

Open a pull request, and reference the initial issue in the pull request message (e.g. fixes #). 
Write a good description and title, so everybody will know what is fixed/improved.

A Github Action will automatically be triggered to check everything.


### Wait for feedback

Before accepting your contributions, we will review them. You may get feedback about what should be 
fixed in your modified code. If so, just keep committing in your branch and the pull request will be 
updated automatically.

We will only accept pull requests that have zero failing checks.


### The merge

Finally, your contributions will be merged.

The merge will automatically publish a new snapshot version.
A release version will be published manually by the [core team](#contact-the-edison-core-team) according to our roadmap.  
