#!/usr/bin/env python
# -*- coding: utf-8 -*-

from setuptools import setup

with open('../README.rst') as readme_file:
    readme = readme_file.read()

requirements = [
    'docutils~=0.22.4',
    'click~=8.4.2',
    'sphinx~=9.1.0',
    'ghp-import~=2.1.0',
    'sphinxcontrib-fulltoc~=1.2.0',
    'sphinxcontrib-jsdemo~=0.1.4',
    'numpy~=2.5.2',
    'pandas~=3.0.5',
    'future~=1.0.0',
    'networkx~=3.6.1',
    'matplotlib~=3.11.1',
    'pyomo~=6.10.1',
    'pypower~=5.1.21'
]

test_requirements = [

]

setup(
    name='psst',
    version='0.1.10',
    description="Power System Simulation Toolbox",
    long_description=readme,
    author="Dheepak Krishnamurthy",
    author_email='kdheepak89@gmail.com',
    url='https://github.com/power-system-simulation-toolbox/psst',
    packages=[
        'psst',
    ],
    package_dir={'psst':
                     'psst'},
    entry_points={
        'console_scripts': [
            'psst=psst.cli:cli'
        ]
    },
    include_package_data=True,
    install_requires=requirements,
    license="MIT license",
    zip_safe=False,
    keywords='psst',
    classifiers=[
        'Development Status :: 4 - Beta',
        'Intended Audience :: Developers',
        'License :: OSI Approved :: MIT License',
        'Natural Language :: English',
        "Programming Language :: Python",
        'Programming Language :: Python :: 3.12',
        "Topic :: Scientific/Engineering"
    ],
    test_suite='tests',
    tests_require=test_requirements
)
