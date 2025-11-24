from setuptools import setup, find_packages

with open("README.md", encoding="utf-8") as f:
    long_description = f.read()

setup(
    name="facebook-rental-crawler",
    version="1.0.0",
    packages=find_packages(),
    install_requires=[
        "selenium>=4.20.0",
        "pymongo>=4.8.0",
        "requests>=2.31.0",
        "python-dotenv>=1.0.0",
    ],
    python_requires=">=3.8",
    author="JessYu-1011, hding4915",
    description="Facebook Group Rental Crawler powered by Selenium",
    long_description=long_description,
    long_description_content_type="text/markdown",
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
    ],
)
